import { For, Show } from "solid-js";
import { DateTime } from 'luxon';

const Room = (props: { room: Room }) => {
    const { room } = props;
    return (
        <li class="overflow-hidden rounded-xl border border-gray-200">
            <div class="flex items-center gap-x-4 border-b border-gray-900/5 bg-gray-50 p-6">
                <div class="text-lg font-medium leading-6 text-gray-900">Name: {room.name}</div>
            </div>
            <dl class="-my-3 divide-y divide-gray-100 px-6 py-4 text-sm leading-6">
                <Show when={room.data}>
                    <div class="flex justify-between gap-x-4 py-3">
                        <dt class="text-black text-sm font-bold">Temperature</dt>
                        <dd class="text-gray-500 text-sm">{room.data.temperature_mc / 10}°C</dd>
                    </div>
                    <div class="flex justify-between gap-x-4 py-3">
                        <dt class="text-black text-sm font-bold">Humidity</dt>
                        <dd class="text-gray-500 text-sm">{room.data.humidity}%</dd>
                    </div>
                    <div class="flex justify-between gap-x-4 py-3">
                        <dt class="text-black text-sm font-bold">Last Alive</dt>
                        <dd class="text-gray-500 text-sm">{DateTime.fromISO(room.data.time).toRelative()}</dd>
                    </div>
                </Show>
                <div class="flex justify-between gap-x-4 py-3">
                    <dt class="text-black text-sm font-bold">Devices</dt>
                    <dd class="text-gray-700 flex justify-between gap-x-1">
                <For each={room.devices}>
                    {(device) =>
                            <div>{device.id}</div>
                   
                    }
                </For>
                </dd>
                </div>
            </dl>
        </li>
    )
}

export { Room }
