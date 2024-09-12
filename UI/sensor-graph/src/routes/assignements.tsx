import { Show, createResource } from "solid-js";
import { Assignements } from "~/components/Assignements";
import { getAssignements } from "~/API/api";

export default function assignementsPage() {
  const [serverDevices, {mutate, refetch}] = createResource(getAssignements)
  return (
    <main class="text-center mx-auto text-gray-700 p-4">
      <Show when={serverDevices()} fallback={<div>Loading...</div>}>
        {(data) => <Assignements data={data()} refetch={refetch} />}
      </Show>
    </main>
  );
}
