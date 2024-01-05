import { For, createSignal, batch, JSXElement, createResource } from "solid-js";
import {
  DragDropProvider,
  DragDropSensors,
  DragOverlay,
  Draggable,
  Droppable,
  createDroppable,
  closestCenter,
  createDraggable,
} from "@thisbeyond/solid-dnd";
import { Icon } from "solid-heroicons";
import { plus } from "solid-heroicons/solid";
import { createStore } from "solid-js/store";
import { AddRoomModal } from "./AddRoomModal";

const DraggableDevice = (props: { id: number, label: JSXElement }) => {
  const draggable = createDraggable(props.id);
  return (<div use:draggable class="inline-flex items-center place-content-center text-center cursor-grab rounded-md bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-700/10">
    {props.label}
  </div>);
}

const Room = (props: { id: string, items: number[], getLabel: (id: number) => JSXElement }) => {
  const droppable = createDroppable(props.id);

  const activeClass = () => {
    if (droppable.isActiveDroppable) {
      return "ring-2 ring-indigo-500 ring-offset-2"
    }
    return "";
  };

  let titlePrefix = props.id != "unassigned" ? "Location: " : "";

  let extraClasses = props.id == "unassigned" ? "italic text-gray-500" : "text-gray-900";
  return (
    <div use:droppable class={`min-h-32 rounded-lg border border-gray-300 bg-white px-6 shadow-sm ${activeClass()}`}>
      <div class="border-b border-gray-200 bg-white px-2 py-2 sm:px-6">
        <h3 class={`text-base font-semibold leading-6 ${extraClasses}`}>{`${titlePrefix}`}{props.id}</h3>
      </div>
      <div class="grid grid-cols-3 gap-1 sm:grid-cols-3 py-2 min-h-[48px]">
        <For each={props.items}>
          {(item) => <DraggableDevice id={item} label={props.getLabel(item)}></DraggableDevice>}
        </For>
      </div>
    </div>
  );
};

const Assignements = (props) => {
  const [showModal, setShowModal] = createSignal(false);

  const initialStore: { [index: string]: number[] } = {};
  const [containers, setContainers] = createStore<Record<string, number[]>>(initialStore);

  const unassigned = "unassigned";
  const labels = new Map<number, string>();

  const setData = (serverData: Assignements) => {
    console.log("Re-setting containers")
    batch(() => {
      setContainers(unassigned, [])
      let count = 0
      for (let device of serverData.unassignedDevices) {
        setContainers(unassigned, (items) => [...items, count])
        labels.set(count, device.id);
        count += 1
      }

      for (let location of serverData.locations) {
        setContainers(location.name, (items) => [])
        if (!location.devices) { continue }
        for (let device of location.devices) {
          setContainers(location.name, (items) => [...items, count])
          labels.set(count, device.id);
          count += 1
        }
      }
    })
  }

  const getLabel = (id: number) => <span>{labels.get(id)}</span>

  const containerIds = () => Object.keys(containers);

  const isContainer = (id: string) => containerIds().includes(id);

  const getContainer = (id: string) => {
    for (const [key, items] of Object.entries(containers)) {
      if (items.includes(id)) {
        return key;
      }
    }
  };

  const closestContainerOrItem = (draggable: Draggable, droppables: Droppable[], context) => {
    const closestContainer = closestCenter(
      draggable,
      droppables.filter((droppable) => isContainer(droppable.id)),
      context
    );
    if (closestContainer) {
      const containerItemIds = containers[closestContainer.id];
      const closestItem = closestCenter(
        draggable,
        droppables.filter((droppable) =>
          containerItemIds.includes(droppable.id)
        ),
        context
      );
      if (!closestItem) {
        return closestContainer;
      }

      if (getContainer(draggable.id) !== closestContainer.id) {
        const isLastItem =
          containerItemIds.indexOf(closestItem.id as number) ===
          containerItemIds.length - 1;

        if (isLastItem) {
          const belowLastItem =
            draggable.transformed.center.y > closestItem.transformed.center.y;

          if (belowLastItem) {
            return closestContainer;
          }
        }
      }
      return closestItem;
    }
  };

  const move = (draggable, droppable, onlyWhenChangingContainer = true) => {
    const draggableContainer = getContainer(draggable.id);
    const droppableContainer = isContainer(droppable.id)
      ? droppable.id
      : getContainer(droppable.id);

    if (
      draggableContainer != droppableContainer ||
      !onlyWhenChangingContainer
    ) {
      const containerItemIds = containers[droppableContainer];
      let index = containerItemIds.indexOf(droppable.id);
      if (index === -1) index = containerItemIds.length;

      batch(() => {
        setContainers(draggableContainer, (items) =>
          items.filter((item) => item !== draggable.id)
        );
        setContainers(droppableContainer, (items) => [...items, draggable.id]);
      });
    }
  };


  const onDragOver = ({ draggable, droppable }) => {
    if (draggable && droppable) {
      move(draggable, droppable);
    }
  };

  const onDragEnd = ({ draggable, droppable }) => {
    if (draggable && droppable) {
      move(draggable, droppable, false);
    }
  };

  const refreshAssignements = async () => {
    const assignements = await props.refetch();
    if (assignements) {
      setData(assignements)
    }
  }

  setData(props.data)

  return <div>
        <DragDropProvider
          onDragOver={onDragOver}
          onDragEnd={onDragEnd}
          collisionDetector={closestContainerOrItem}
        >
          <DragDropSensors />
          <div role="list" class="grid grid-cols-1 gap-x-6 gap-y-8 lg:grid-cols-3 xl:gap-x-8">
            <For each={containerIds()}>
              {(key) => <div class="rounded-xl"><Room id={key} items={containers[key]} getLabel={getLabel} /></div>}
            </For>
            <div class="rounded-xl min-h-[12px] border-gray-200 ">
              <div class="grid px-2 py-2 sm:px-6 min-h-32 place-items-center rounded-lg border border-dashed border-gray-300 bg-white">
                <button
                  type="button"
                  class="rounded-full bg-blue-700 p-2 text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                  onClick={() => setShowModal(true)}>
                  <Icon path={plus} class="h-12 w-12" />
                </button>
              </div>
            </div>
          </div>
          <DragOverlay>
            {(draggable) => <div class="inline-flex items-center rounded-md cursor-grabbing bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-700/10 opacity-70">
              {getLabel(draggable.id)}
            </div>}
          </DragOverlay>
        </DragDropProvider>
        <AddRoomModal showModal={showModal} setShowModal={setShowModal} refetch={refreshAssignements}></AddRoomModal>
      </div>;
}

export { Assignements }