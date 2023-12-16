import { Show } from "solid-js";
import { Devices } from "~/components/Devices";
import { useRouteData } from "solid-start";
import { createServerData$ } from "solid-start/server";
import { getDevicesAPIURL } from "~/API/api";

export function routeData() {
  return createServerData$(async () => {
    const response = await fetch(getDevicesAPIURL());
    return await response.json() as Devices;
  });
}

export default function roomsPage() {
  const serverDevices = useRouteData<typeof routeData>();

  return (
    <main class="text-center mx-auto text-gray-700 p-4">
      <Show when={serverDevices()} fallback={<div>Loading...</div>}>
      {(data) => <Devices data={data()} />}
      </Show>
    </main>
  );
}
