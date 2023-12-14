import { For, createSignal, batch, JSXElement } from "solid-js";
import { Device } from "~/components/Device";
import {
  DragDropProvider,
  DragDropSensors,
  DragEventHandler,
  DragOverlay,
  Draggable,
  Droppable,
  createDroppable,
  closestCenter,
  createDraggable,
} from "@thisbeyond/solid-dnd";
import { createStore } from "solid-js/store";

const DraggableDevice = (props) => {
  const draggable = createDraggable(props.id);
  return  <li class="px-6 py-4">
    <div use:draggable class="inline-flex items-center cursor-grab rounded-md bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-700/10">
        {props.label}
    </div>
  </li>;
}

const Room = (props: {id: string, items: number[], getLabel: (id: number) => JSXElement}) => {
  const droppable = createDroppable(props.id);
  return (
    <div use:droppable class="overflow-hidden rounded-md bg-white shadow">
      <div>{props.id}</div>
      <ul role="list" class="divide-y divide-gray-200">
        <For each={props.items}>
          {(item) => <DraggableDevice id={item} label={props.getLabel(item)}></DraggableDevice>}
        </For>
      </ul>
    </div>
  );
};


const Devices = (props: { data: Devices | undefined }) => {
  if (props.data == undefined) {
    return <div>Missing Devices Data</div>
  }
  const devices = props.data
  const initialStore: {string: number[]} = {};
  const unassigned = "Unassigned";
  const labels = new Map<number, string>();
  let count = 0
  for (let device of devices) {
    const location: string = device.location || unassigned;
    if (initialStore[location] == undefined) {initialStore[location] = []}
    initialStore[location].push(count)
    labels.set(count, device.id);
    count+=1
  }
  const [containers, setContainers] = createStore<Record<string, number[]>>(initialStore);
  
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
  return (
    <div>
      <DragDropProvider
        onDragOver={onDragOver}
        onDragEnd={onDragEnd}
        collisionDetector={closestContainerOrItem}
      >
        <DragDropSensors />
        <ul role="list" class="grid grid-cols-1 gap-x-6 gap-y-8 lg:grid-cols-3 xl:gap-x-8">
          <For each={containerIds()}>
            {(key) => <li class="overflow-hidden rounded-xl border border-gray-200"><Room id={key} items={containers[key]} getLabel={getLabel} /></li>}
          </For>
        </ul>
        <DragOverlay>
          {(draggable) => <div class="inline-flex items-center rounded-md cursor-grabbing bg-blue-50 px-2 py-1 text-xs font-medium text-blue-700 ring-1 ring-inset ring-blue-700/10">
            {getLabel(draggable.id)}
            </div>}
        </DragOverlay>
      </DragDropProvider>
    </div>
  );
}

export { Devices }