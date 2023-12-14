import { Show } from "solid-js";

const Device = (props: { device: Device }) => {
    const { device } = props;
    return (
        <li class="overflow-hidden rounded-xl border border-gray-200">
            <div class="flex items-center gap-x-4 border-b border-gray-900/5 bg-gray-50 p-6">
                <div class="text-lg font-medium leading-6 text-gray-900">Name: {device.id}</div>
            </div>
            <dl class="-my-3 divide-y divide-gray-100 px-6 py-4 text-sm leading-6">
                <Show when={device.location}>
                    <div class="flex justify-between gap-x-4 py-3">
                        <dt class="text-black text-sm font-bold">Location</dt>
                        <dd class="text-gray-500 text-sm">{device.location}</dd>
                    </div>
                   
                </Show>
            </dl>
        </li>
    )
}

export { Device }
