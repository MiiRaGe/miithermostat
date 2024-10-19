import { createResource, Show } from "solid-js";
import { Rooms } from "~/components/Rooms";
import { getRooms } from "~/API/rooms";
import { createAsync } from "@solidjs/router";

export default function roomsPage() {
  const [serverRooms] = createResource(() => getRooms());

  return (
    <main class="text-center mx-auto text-gray-700 p-4">
      <Show when={serverRooms()} fallback={<div>Loading...</div>}>
      {(data) => <Rooms data={data()} />}
      </Show>
    </main>
  );
}
