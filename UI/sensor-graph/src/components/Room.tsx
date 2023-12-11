import { For } from "solid-js";
import roomsPage from "~/routes/rooms";

const Room = (props: { room: Room }) => {
    const { room } = props;
    return (
        <li class="overflow-hidden rounded-xl border border-gray-200">
            <div class="flex items-center gap-x-4 border-b border-gray-900/5 bg-gray-50 p-6">
                <div class="text-lg font-medium leading-6 text-gray-900">Name: {room.name}</div>
            </div>
            <dl class="-my-3 divide-y divide-gray-100 px-6 py-4 text-sm leading-6">
                <div class="flex justify-between gap-x-4 py-3">
                    <dt class="text-black text-sm font-bold">Device Id</dt>
                    <dd class="text-black text-sm font-bold">Last Alive</dd>
                </div>
                <For each={room.devices}>
                    {(device) =>
                        <div class="flex justify-between gap-x-4 py-3">
                            <dt class="text-gray-500">{device.id}</dt>
                            <dd class="text-gray-700"><time datetime="2022-12-13">December 13, 2022</time></dd>
                        </div>
                    }
                </For>
            </dl>
        </li>
    )
}

export { Room }