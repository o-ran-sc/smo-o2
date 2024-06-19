#https://github.com/stackhpc/pyhelm3

import requests
from pyhelm3 import Client
import asyncio

async def test_client():
    #endpoint = "https://httpbin.org/get"
    endpoint = "http://127.0.0.1:8000/api/"
    getresponse = requests.get(endpoint, json={"query":"Hello, World!"})

    #print(getresponse.text)
    print(getresponse.status_code)
    #print(getresponse.json()['message'])
    print(getresponse.json())

async def get_current_version():
    client = Client()
    client = Client(kubeconfig = "/home/fnclab/.kube/config")
    client = Client(executable = "/usr/local/bin/helm")
    # Get the current revision for an existing release
    revision = await client.get_current_revision("hello-world-1711020846", namespace = "default")
    chart_metadata = await revision.chart_metadata()
    print(
        revision.release.name,
        revision.release.namespace,
        revision.revision,
        str(revision.status),
        chart_metadata.name,
        chart_metadata.version
    )

async def list_releases():
    client = Client()
    client = Client(kubeconfig = "/home/fnclab/.kube/config")
    client = Client(executable = "/usr/local/bin/helm")

    # List the deployed releases
    releases = await client.list_releases(all = True, all_namespaces = True)
    for release in releases:
        print("executing for loop ->")
        revision = await release.current_revision()
        print(release.name, release.namespace, revision.revision, str(revision.status)) 

async def uninstall_helm():
    client = Client()
    client = Client(kubeconfig = "/home/fnclab/.kube/config")
    client = Client(executable = "/usr/local/bin/helm")
    revision = await client.get_current_revision("cert-manager", namespace = "default")
    await revision.release.uninstall(wait = True)
    #   Or directly by name
    await client.uninstall_release("cert-manager", namespace = "default", wait = True)

async def install_helm():
    print("executing test_helmClient() ->")

    # This will use the Kubernetes configuration from the environment
    client = Client()
    # Specify the kubeconfig file to use
    client = Client(kubeconfig = "/home/fnclab/.kube/config")
    # Specify a custom Helm executable (by default, we expect 'helm' to be on the PATH)
    client = Client(executable = "/usr/local/bin/helm")
    
    # Fetch a chart
    chart = await client.get_chart(
        "cert-manager",
        repo = "https://charts.jetstack.io",
        version = "v1.8.x"
    )
    print(chart.metadata.name, chart.metadata.version)
    #print(await chart.readme())

    # Install or upgrade a release
    revision = await client.install_or_upgrade_release(
        "cert-manager",
        chart,
        { "installCRDs": True },
        atomic = True,
        wait = True
    )
    print(
        revision.release.name,
        revision.release.namespace,
        revision.revision,
        str(revision.status)
    )
    
if __name__ == "__main__":
    #asyncio.run(get_current_version())
    #asyncio.run(list_releases())
    #asyncio.run(install_helm())
    asyncio.run(uninstall_helm())
