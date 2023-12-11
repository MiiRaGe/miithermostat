import { For } from "solid-js";
import { Room } from "~/components/Room";

const Rooms = (props: { data: Rooms | undefined }) => {
    if (props.data == undefined) {
        return <div>Missing Rooms Data</div>
    }

    const rooms = props.data
    return (
        <ul role="list" class="grid grid-cols-1 gap-x-6 gap-y-8 lg:grid-cols-3 xl:gap-x-8">
            <For each={rooms}>
                {(room) =>
                    <Room room={room}></Room>
                }
            </For>
        </ul>
    )
}

export { Rooms }