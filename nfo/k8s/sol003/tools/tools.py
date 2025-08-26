import json
import requests
from typing import Dict, List, Any
from datetime import datetime


class JSONProcess:

    def expand_dot_notation(self, data):
        """Expand dot notation keys into nested dictionaries with Helm-style array support"""
        result = {}

        for key, value in data.items():
            keys = key.split(".")
            current = result

            for i, k in enumerate(keys):
                # Handle array notation like hosts[0], paths[1], etc.
                if "[" in k and "]" in k:
                    base_key = k.split("[")[0]
                    index = int(k.split("[")[1].split("]")[0])

                    # Initialize array if it doesn't exist
                    if base_key not in current:
                        current[base_key] = []
                    # Extend array to required index
                    while len(current[base_key]) <= index:
                        current[base_key].append(None)

                    # If this is the last key, set the value directly in the array
                    if i == len(keys) - 1:
                        current[base_key][index] = value
                    else:
                        # Not last key, create dict for further nesting
                        if current[base_key][index] is None:
                            current[base_key][index] = {}
                        current = current[base_key][index]
                else:
                    # Regular key handling
                    if i == len(keys) - 1:
                        current[k] = value
                    else:
                        if k not in current:
                            current[k] = {}
                        current = current[k]
        print("EXPAND DOT NOTATION")
        print(result)
        return result


class KubernetesResourceMetrics:
    def __init__(self, prometheus_url: str):
        """Initialize with Prometheus server URL."""
        self.base_url = prometheus_url.rstrip("/")
        self.query_endpoint = f"{self.base_url}/api/v1/query"

        # Kubernetes resource notation reference
        self.k8s_notation = {
            "cpu": {
                "unit": "millicores",  # 1 CPU = 1000m (millicores)
                "description": "1000m = 1 CPU core/thread, 100m = 0.1 CPU",
            },
            "memory": {
                "unit": "bytes",
                "conversions": {
                    "Ki": 1024,  # Kibibytes
                    "Mi": 1024**2,  # Mebibytes
                    "Gi": 1024**3,  # Gibibytes
                    "K": 1000,  # Kilobytes
                    "M": 1000**2,  # Megabytes
                    "G": 1000**3,  # Gigabytes
                },
            },
        }

    def _query(self, promql: str) -> List[Dict]:
        """Execute query and return parsed results."""
        try:
            response = requests.get(self.query_endpoint, params={"query": promql})
            response.raise_for_status()
            result = response.json()

            if not result or "data" not in result:
                return []

            parsed = []
            for item in result.get("data", {}).get("result", []):
                parsed.append(
                    {
                        "labels": item.get("metric", {}),
                        "value": float(item.get("value", [0, 0])[1]),
                        "timestamp": datetime.fromtimestamp(
                            item.get("value", [0, 0])[0]
                        ),
                    }
                )
            return parsed
        except Exception as e:
            print(f"Query failed: {e}")
            return []

    def get_node_resources(self, resource_type: str = "all") -> Dict:
        """Get node resources - allocatable, capacity, used, available."""
        queries = {
            # CPU in cores (Kubernetes shows as integer cores, not millicores for node capacity)
            "cpu_capacity": 'kube_node_status_capacity{resource="cpu"}',
            "cpu_allocatable": 'kube_node_status_allocatable{resource="cpu"}',
            "cpu_requested": 'sum by (node) (kube_pod_container_resource_requests{resource="cpu"})',
            "cpu_used": 'sum by (instance) (1 - avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])))',
            # Memory in bytes
            "memory_capacity": 'kube_node_status_capacity{resource="memory"}',
            "memory_allocatable": 'kube_node_status_allocatable{resource="memory"}',
            "memory_requested": 'sum by (node) (kube_pod_container_resource_requests{resource="memory"})',
            "memory_used": "node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes",
            "memory_available": "node_memory_MemAvailable_bytes",
            # Pods count
            "pods_capacity": 'kube_node_status_capacity{resource="pods"}',
            "pods_allocatable": 'kube_node_status_allocatable{resource="pods"}',
            "pods_running": "count by (node) (kube_pod_info)",
            # Additional hardware info
            "cpu_threads": 'count by (instance) (node_cpu_seconds_total{mode="idle"})',  # Total CPU threads
            "network_interfaces": "count by (instance) (node_network_info)",  # Network interfaces count
        }

        if resource_type != "all":
            queries = {k: v for k, v in queries.items() if resource_type in k}

        results = {}
        for metric_name, query in queries.items():
            data = self._query(query)
            results[metric_name] = data

        return self._calculate_available(results)

    def _calculate_available(self, results: Dict) -> Dict:
        """Calculate available resources from raw metrics."""
        nodes = {}

        # Process each metric
        for metric_name, data_list in results.items():
            for data in data_list:
                # Get node identifier
                node = data["labels"].get("node") or data["labels"].get(
                    "instance", "unknown"
                )
                if node not in nodes:
                    nodes[node] = {}

                # Convert CPU values to millicores for consistency
                if "cpu" in metric_name and metric_name != "cpu_threads":
                    if "capacity" in metric_name or "allocatable" in metric_name:
                        # These are in cores, convert to millicores
                        nodes[node][metric_name + "_millicores"] = data["value"] * 1000
                    elif "requested" in metric_name:
                        # Already in cores from sum, convert to millicores
                        nodes[node][metric_name + "_millicores"] = data["value"] * 1000
                    elif "used" in metric_name:
                        # This is a ratio (0-1), convert to percentage
                        nodes[node]["cpu_used_percent"] = data["value"] * 100

                # Store raw value
                nodes[node][metric_name] = data["value"]

        # Calculate available resources for each node
        for node, metrics in nodes.items():
            # CPU Available (in millicores)
            if (
                "cpu_allocatable_millicores" in metrics
                and "cpu_requested_millicores" in metrics
            ):
                metrics["cpu_available_millicores"] = metrics[
                    "cpu_allocatable_millicores"
                ] - metrics.get("cpu_requested_millicores", 0)
                metrics["cpu_available_cores"] = (
                    metrics["cpu_available_millicores"] / 1000
                )

            # Memory Available (already calculated by node exporter)
            if "memory_allocatable" in metrics and "memory_requested" in metrics:
                metrics["memory_unrequested_bytes"] = metrics[
                    "memory_allocatable"
                ] - metrics.get("memory_requested", 0)
                metrics["memory_unrequested_gi"] = metrics[
                    "memory_unrequested_bytes"
                ] / (1024**3)

            if "memory_available" in metrics:
                metrics["memory_available_gi"] = metrics["memory_available"] / (1024**3)

            # Pods Available
            if "pods_allocatable" in metrics and "pods_running" in metrics:
                metrics["pods_available"] = metrics["pods_allocatable"] - metrics.get(
                    "pods_running", 0
                )

        return nodes

    def get_network_details(self) -> List[Dict]:
        """Get detailed network interface information - physical NICs only."""
        queries = {
            "interfaces": "node_network_info",
            "interface_up": "node_network_up",
            "receive_bytes_rate": "rate(node_network_receive_bytes_total[5m])",
            "transmit_bytes_rate": "rate(node_network_transmit_bytes_total[5m])",
            "interface_speed": "node_network_speed_bytes",
        }

        network_data = {}
        for metric_name, query in queries.items():
            for result in self._query(query):
                instance = result["labels"].get("instance", "unknown")
                device = result["labels"].get("device", "unknown")

                if instance not in network_data:
                    network_data[instance] = {}
                if device not in network_data[instance]:
                    network_data[instance][device] = {}

                network_data[instance][device][metric_name] = result["value"]

                # Add interface details from labels
                if metric_name == "interfaces":
                    network_data[instance][device].update(
                        {
                            "address": result["labels"].get("address"),
                            "device": result["labels"].get("device"),
                            "operstate": result["labels"].get("operstate"),
                        }
                    )

        # Virtual interface patterns to exclude
        virtual_patterns = [
            "lo",
            "docker",
            "veth",
            "virbr",
            "vir",
            "tun",
            "tap",
            "cali",
            "flannel",
            "cni",
            "kube",
            "dummy",
            "bond",
            "br-",
            "vxlan",
            "weave",
            "datapath",
        ]

        # Flatten for easier reading - physical NICs only
        flattened = []
        for instance, devices in network_data.items():
            for device, metrics in devices.items():
                # Skip virtual interfaces
                is_virtual = any(
                    device.startswith(pattern) for pattern in virtual_patterns
                )
                if is_virtual:
                    continue

                # Physical NICs should have speed > 0 and valid MAC
                speed = metrics.get("interface_speed", 0)
                mac = metrics.get("address", "")

                # Only include if has meaningful speed (>= 1Gbps) or valid MAC
                if speed >= 125000000 or (
                    mac and mac != "00:00:00:00:00:00"
                ):  # 125MB/s = 1Gbps
                    flattened.append(
                        {
                            "node": instance,
                            "interface": device,
                            "up": metrics.get("interface_up", 0) == 1,
                            "speed_gbps": speed * 8 / (1000**3) if speed else 0,
                            "rx_mbps": metrics.get("receive_bytes_rate", 0)
                            * 8
                            / (1000**2),
                            "tx_mbps": metrics.get("transmit_bytes_rate", 0)
                            * 8
                            / (1000**2),
                            "mac": mac,
                            "state": metrics.get("operstate"),
                            "physical": True,
                        }
                    )

        return flattened

    def get_cluster_summary(self) -> Dict:
        """Get complete cluster resource summary with K8s notation and NIC capabilities."""
        node_resources = self.get_node_resources()
        network = self.get_network_details()

        # Add NIC capabilities check
        nic_capabilities = self.check_nic_capabilities()

        # Calculate cluster totals
        totals = {
            "cpu": {
                "capacity_cores": sum(
                    n.get("cpu_capacity", 0) for n in node_resources.values()
                ),
                "allocatable_cores": sum(
                    n.get("cpu_allocatable", 0) for n in node_resources.values()
                ),
                "available_millicores": sum(
                    n.get("cpu_available_millicores", 0)
                    for n in node_resources.values()
                ),
                "requested_millicores": sum(
                    n.get("cpu_requested_millicores", 0)
                    for n in node_resources.values()
                ),
                "cpu_threads_total": sum(
                    n.get("cpu_threads", 0) for n in node_resources.values()
                ),
            },
            "memory": {
                "capacity_gi": sum(
                    n.get("memory_capacity", 0) for n in node_resources.values()
                )
                / (1024**3),
                "allocatable_gi": sum(
                    n.get("memory_allocatable", 0) for n in node_resources.values()
                )
                / (1024**3),
                "available_gi": sum(
                    n.get("memory_available", 0) for n in node_resources.values()
                )
                / (1024**3),
                "unrequested_gi": sum(
                    n.get("memory_unrequested_bytes", 0)
                    for n in node_resources.values()
                )
                / (1024**3),
            },
            "pods": {
                "capacity": sum(
                    n.get("pods_capacity", 0) for n in node_resources.values()
                ),
                "running": sum(
                    n.get("pods_running", 0) for n in node_resources.values()
                ),
                "available": sum(
                    n.get("pods_available", 0) for n in node_resources.values()
                ),
            },
            "network": {
                "total_interfaces": len(network),
                "active_interfaces": len([n for n in network if n["up"]]),
                "total_bandwidth_gbps": sum(n["speed_gbps"] for n in network),
                "dpdk_capable_nodes": nic_capabilities["cluster_capabilities"][
                    "dpdk_nodes"
                ],
                "sriov_capable_nodes": nic_capabilities["cluster_capabilities"][
                    "sriov_nodes"
                ],
                "total_dpdk_nics": nic_capabilities["cluster_capabilities"][
                    "total_dpdk_nics"
                ],
                "total_sriov_nics": nic_capabilities["cluster_capabilities"][
                    "total_sriov_nics"
                ],
                "dpdk_ready": len(nic_capabilities["recommendations"]["dpdk_ready"]),
                "sriov_ready": len(nic_capabilities["recommendations"]["sriov_ready"]),
            },
        }

        # Merge NIC capabilities into per-node data
        for node_name in node_resources.keys():
            if node_name in nic_capabilities["per_node"]:
                node_resources[node_name]["nic_capabilities"] = {
                    "dpdk_supported": nic_capabilities["per_node"][node_name][
                        "dpdk_supported"
                    ],
                    "sriov_supported": nic_capabilities["per_node"][node_name][
                        "sriov_supported"
                    ],
                    "dpdk_nics": nic_capabilities["per_node"][node_name][
                        "dpdk_capable"
                    ],
                    "sriov_nics": nic_capabilities["per_node"][node_name][
                        "sriov_capable"
                    ],
                    "hugepages_configured": nic_capabilities["per_node"][node_name][
                        "hugepages_configured"
                    ],
                    "iommu_enabled": nic_capabilities["per_node"][node_name][
                        "iommu_enabled"
                    ],
                }

        # Add K8s notation explanation
        totals["k8s_notation"] = self.k8s_notation

        return {
            "cluster_totals": totals,
            "per_node": node_resources,
            "network_interfaces": network,
            "nic_features": {
                "dpdk_ready_nodes": nic_capabilities["recommendations"]["dpdk_ready"],
                "sriov_ready_nodes": nic_capabilities["recommendations"]["sriov_ready"],
                "config_needed": {
                    "dpdk": nic_capabilities["recommendations"]["dpdk_needs_config"],
                    "sriov": nic_capabilities["recommendations"]["sriov_needs_config"],
                },
            },
            "readable_summary": self._format_readable_summary(totals, nic_capabilities),
        }

    def _format_readable_summary(
        self, totals: Dict, nic_capabilities: Dict = None
    ) -> Dict:
        """Format a human-readable summary with NIC capabilities."""
        cpu_total = totals["cpu"]
        mem_total = totals["memory"]
        net_total = totals["network"]

        summary = {
            "cpu": f"{cpu_total['available_millicores']:.0f}m available "
            f"({cpu_total['available_millicores']/1000:.2f} cores) "
            f"out of {cpu_total['allocatable_cores']:.0f} allocatable cores "
            f"({cpu_total['cpu_threads_total']:.0f} total threads)",
            "memory": f"{mem_total['available_gi']:.2f}Gi available "
            f"out of {mem_total['allocatable_gi']:.2f}Gi allocatable",
            "pods": f"{totals['pods']['available']:.0f} pod slots available",
            "network": f"{net_total['active_interfaces']} active NICs "
            f"with {net_total['total_bandwidth_gbps']:.1f}Gbps total bandwidth",
        }

        # Add NIC features summary
        if net_total.get("dpdk_ready", 0) > 0 or net_total.get("sriov_ready", 0) > 0:
            features = []
            if net_total.get("dpdk_ready", 0) > 0:
                features.append(f"{net_total['dpdk_ready']} DPDK-ready nodes")
            if net_total.get("sriov_ready", 0) > 0:
                features.append(f"{net_total['sriov_ready']} SR-IOV-ready nodes")
            summary["advanced_networking"] = f"Supports: {', '.join(features)}"
        else:
            summary["advanced_networking"] = "No DPDK/SR-IOV ready nodes"

        return summary

    def check_nic_capabilities(self) -> Dict:
        """Check NIC support for DPDK and SR-IOV - physical NICs only."""

        # Common queries to detect capabilities
        queries = {
            # PCI devices and drivers - physical NICs have PCI addresses
            "pci_devices": "node_network_info",
            # SR-IOV Virtual Functions
            "sriov_vf_total": "node_network_sriov_vf_total",
            "sriov_vf_used": "node_network_sriov_vf",
            # Hugepages (required for DPDK)
            "hugepages_total": "node_memory_HugePages_Total",
            "hugepages_free": "node_memory_HugePages_Free",
            # IOMMU groups (for SR-IOV/VFIO)
            "iommu_groups": "node_iommu_groups_total",
            # Physical NIC speed (virtual interfaces usually don't report speed)
            "nic_speed": "node_network_speed_bytes",
        }

        results = {}
        for metric_name, query in queries.items():
            results[metric_name] = self._query(query)

        # Build speed lookup for physical NIC detection
        physical_nics = {}
        for item in results.get("nic_speed", []):
            node = item["labels"].get("instance", "unknown")
            device = item["labels"].get("device", "")
            speed = item["value"]

            if node not in physical_nics:
                physical_nics[node] = {}

            # Physical NICs typically have speed > 0
            if speed > 0:
                physical_nics[node][device] = speed

        # Analyze capabilities per node
        node_capabilities = {}

        # Virtual interface patterns to exclude
        virtual_patterns = [
            "lo",
            "docker",
            "veth",
            "virbr",
            "vir",
            "tun",
            "tap",
            "cali",
            "flannel",
            "cni",
            "kube",
            "dummy",
            "bond",
            "br-",
            "vxlan",
            "weave",
            "datapath",
        ]

        for item in results.get("pci_devices", []):
            node = item["labels"].get("instance", "unknown")
            device = item["labels"].get("device", "")
            driver = item["labels"].get("driver", "")
            address = item["labels"].get("address", "")

            # Skip if device matches virtual patterns
            is_virtual = any(device.startswith(pattern) for pattern in virtual_patterns)
            if is_virtual:
                continue

            # Additional check: physical NICs usually have MAC addresses and proper drivers
            has_mac = address and address != "00:00:00:00:00:00"
            has_physical_driver = driver and driver not in ["bridge", "tun", "veth"]

            # Check if this device has a reported speed (indicates physical NIC)
            has_speed = (
                node in physical_nics
                and device in physical_nics[node]
                and physical_nics[node][device] > 0
            )

            # Only process if likely physical NIC
            if not (has_mac and has_physical_driver) and not has_speed:
                continue

            if node not in node_capabilities:
                node_capabilities[node] = {
                    "interfaces": [],
                    "dpdk_capable": [],
                    "sriov_capable": [],
                    "dpdk_supported": False,
                    "sriov_supported": False,
                    "hugepages_configured": False,
                    "iommu_enabled": False,
                }

            # Physical NIC drivers that support DPDK/SR-IOV
            dpdk_drivers = [
                "igb_uio",
                "vfio-pci",
                "uio_pci_generic",
                "ixgbe",
                "i40e",
                "ice",
                "igb",
                "e1000e",
                "e1000",
                "mlx4_core",
                "mlx5_core",
                "mlx4_en",
                "mlx5_en",
                "bnx2x",
                "bnxt_en",
                "qede",
                "nfp",
            ]

            sriov_drivers = [
                "ixgbe",
                "i40e",
                "ice",
                "igb",
                "mlx4_core",
                "mlx5_core",
                "mlx4_en",
                "mlx5_en",
                "bnx2x",
                "bnxt_en",
                "qede",
                "nfp",
                "enic",  # Cisco VIC
            ]

            # Get speed in Gbps
            speed_bytes = physical_nics.get(node, {}).get(device, 0)
            speed_gbps = speed_bytes * 8 / (1000**3) if speed_bytes else 0

            interface_info = {
                "device": device,
                "driver": driver,
                "dpdk_compatible": False,
                "sriov_compatible": False,
                "speed_gbps": speed_gbps,
                "mac": address,
                "state": item["labels"].get("operstate", ""),
                "physical": True,
            }

            # Check compatibility based on driver
            if driver in dpdk_drivers:
                interface_info["dpdk_compatible"] = True
                node_capabilities[node]["dpdk_capable"].append(device)
                node_capabilities[node]["dpdk_supported"] = True

            if driver in sriov_drivers:
                interface_info["sriov_compatible"] = True
                node_capabilities[node]["sriov_capable"].append(device)

            # Only add physical interfaces with meaningful speed or known physical drivers
            if speed_gbps >= 1.0 or driver in (dpdk_drivers + sriov_drivers):
                node_capabilities[node]["interfaces"].append(interface_info)

        # Check SR-IOV VFs if available
        for item in results.get("sriov_vf_total", []):
            node = item["labels"].get("instance", "unknown")
            if node in node_capabilities:
                node_capabilities[node]["sriov_supported"] = True
                node_capabilities[node]["sriov_vf_total"] = int(item["value"])

        # Check hugepages (required for DPDK)
        for item in results.get("hugepages_total", []):
            node = item["labels"].get("instance", "unknown")
            if node in node_capabilities and item["value"] > 0:
                node_capabilities[node]["hugepages_configured"] = True
                node_capabilities[node]["hugepages_total"] = int(item["value"])

        for item in results.get("hugepages_free", []):
            node = item["labels"].get("instance", "unknown")
            if node in node_capabilities:
                node_capabilities[node]["hugepages_free"] = int(item["value"])

        # Check IOMMU
        for item in results.get("iommu_groups", []):
            node = item["labels"].get("instance", "unknown")
            if node in node_capabilities and item["value"] > 0:
                node_capabilities[node]["iommu_enabled"] = True
                node_capabilities[node]["iommu_groups"] = int(item["value"])

        # Generate summary - only count physical NICs
        summary = {
            "cluster_capabilities": {
                "dpdk_nodes": sum(
                    1 for n in node_capabilities.values() if n["dpdk_supported"]
                ),
                "sriov_nodes": sum(
                    1 for n in node_capabilities.values() if n["sriov_supported"]
                ),
                "total_dpdk_nics": sum(
                    len(n["dpdk_capable"]) for n in node_capabilities.values()
                ),
                "total_sriov_nics": sum(
                    len(n["sriov_capable"]) for n in node_capabilities.values()
                ),
                "total_physical_nics": sum(
                    len(n["interfaces"]) for n in node_capabilities.values()
                ),
                "hugepages_nodes": sum(
                    1 for n in node_capabilities.values() if n["hugepages_configured"]
                ),
                "iommu_nodes": sum(
                    1 for n in node_capabilities.values() if n["iommu_enabled"]
                ),
            },
            "per_node": node_capabilities,
            "recommendations": self._generate_nic_recommendations(node_capabilities),
        }

        return summary

    def _generate_nic_recommendations(self, node_capabilities: Dict) -> Dict:
        """Generate recommendations based on NIC capabilities."""
        recommendations = {
            "dpdk_ready": [],
            "dpdk_needs_config": [],
            "sriov_ready": [],
            "sriov_needs_config": [],
        }

        for node, caps in node_capabilities.items():
            # DPDK readiness
            if caps["dpdk_supported"]:
                if caps["hugepages_configured"]:
                    recommendations["dpdk_ready"].append(
                        {
                            "node": node,
                            "nics": caps["dpdk_capable"],
                            "hugepages_free": caps.get("hugepages_free", 0),
                        }
                    )
                else:
                    recommendations["dpdk_needs_config"].append(
                        {
                            "node": node,
                            "nics": caps["dpdk_capable"],
                            "missing": "hugepages not configured",
                        }
                    )

            # SR-IOV readiness
            if caps["sriov_capable"]:
                if caps.get("iommu_enabled", False):
                    recommendations["sriov_ready"].append(
                        {
                            "node": node,
                            "nics": caps["sriov_capable"],
                            "vf_total": caps.get("sriov_vf_total", 0),
                        }
                    )
                else:
                    recommendations["sriov_needs_config"].append(
                        {
                            "node": node,
                            "nics": caps["sriov_capable"],
                            "missing": "IOMMU not enabled",
                        }
                    )

        return recommendations
