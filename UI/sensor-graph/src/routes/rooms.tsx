import { Show } from "solid-js";
import { Rooms } from "~/components/Rooms";
import { useRouteData } from "solid-start";
import { createServerData$ } from "solid-start/server";
import { getRoomsAPIURL } from "~/API/api";

export function routeData() {
  return createServerData$(async () => {
    const response = await fetch(getRoomsAPIURL());
    return await response.json() as Rooms;
  });
}

export default function roomsPage() {
  const serverRooms = useRouteData<typeof routeData>();

  return (
    <main class="text-center mx-auto text-gray-700 p-4">
      <Show when={serverRooms()} fallback={<div>Loading...</div>}>
      {(data) => <Rooms data={data()} />}
      </Show>
    </main>
  );
}
