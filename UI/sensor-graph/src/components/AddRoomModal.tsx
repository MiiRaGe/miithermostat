import { Portal } from "solid-js/web";
import { Show, batch, createSignal } from "solid-js";
import { createRoom } from "~/API/api";

export const AddRoomModal = (props) => {
    const [name, setName] = createSignal("")
    const [disabled, setDisabled] = createSignal(false)
    const [error, setError] = createSignal("")

    const submitForm = async () => {
        setDisabled(true)
        const {ok, text} = await createRoom(name())
        if (!ok) {
            setDisabled(false)
            setError(`${text}`)
        } else {
            batch(() => {
                setName("")
                props.refetch()
                setDisabled(false)
                props.setShowModal(false)
            })
        }
    }

    return (
        <Portal>
            <Show when={props.showModal()}>
                <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
                    <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>

                    <div class="fixed inset-0 z-10 w-screen overflow-y-auto">
                        <div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
                            <div class="relative transform overflow-hidden rounded-lg bg-white px-4 pb-4 pt-5 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-sm sm:p-6">
                                <div>
                                    <div class="mt-3 text-center sm:mt-5">
                                        <h3 class="text-base font-semibold leading-6 text-gray-900" id="modal-title">Add a new room</h3>
                                        <div class="mt-2">
                                            <p class="text-sm text-gray-500 ">
                                                <input
                                                    autofocus
                                                    name="name"
                                                    type="name"
                                                    placeholder="Name"
                                                    value={name()}
                                                    onChange={(e) => setName(e.currentTarget.value)}
                                                    required
                                                    class="border-indigo-600 border-solid border-2"
                                                />
                                            </p>
                                        </div>
                                    </div>
                                </div>
                                <div>
                                    <Show when={error()}>{error()}</Show>
                                </div>
                                <div class="mt-5 sm:mt-6 sm:grid sm:grid-flow-row-dense sm:grid-cols-2 sm:gap-3">
                                    <button
                                        type="button"
                                        disabled={disabled()}
                                        class="inline-flex w-full justify-center rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 sm:col-start-2"
                                        onClick={submitForm}
                                    >Confirm
                                    </button>
                                    <button
                                        type="button"
                                        class="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:col-start-1 sm:mt-0"
                                        onClick={() => props.setShowModal(false)}
                                    >Cancel</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </Show>

        </Portal>
    )
}