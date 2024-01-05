import { Show, createResource } from "solid-js";
import { Assignements } from "~/components/Assignements";
import { getAssignementsAPIURL } from "~/API/api";

export async function fetchAssignements() {
  const response = await fetch(await getAssignementsAPIURL());
  return await response.json() as Assignements;
}

export default function assignementsPage() {
  const [serverDevices, { mutate, refetch }] = createResource("assignements", fetchAssignements)

  return (
    <main class="text-center mx-auto text-gray-700 p-4">
      <Show when={serverDevices()} fallback={<div>Loading...</div>}>
        {(data) => <Assignements data={data()} refetch={refetch} />}
      </Show>
    </main>
  );
}
