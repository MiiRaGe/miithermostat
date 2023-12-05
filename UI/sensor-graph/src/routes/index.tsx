import { Show, untrack } from "solid-js";
import { SensorGraphs, GraphData } from "~/components/SensorGraphs";
import { A, useRouteData } from "solid-start";
import { createServerData$ } from "solid-start/server";
import { getLastDayMeasurementsAPIURL } from "~/API/api";

type Measurements = Array<{ location: string, time: number, humidity: number, temperature_mc: number }>

export function routeData() {
  return createServerData$(async () => {
    const response = await fetch(getLastDayMeasurementsAPIURL());
    let measurements = await response.json() as Measurements;

    const graphMap: Map<string, Array<{ time: number, humidity: number, temperature: number }>> = new Map();
    for (const { location, time, humidity, temperature_mc } of measurements) {
      if (graphMap.get(location) == undefined) graphMap.set(location, new Array());
      graphMap.get(location)?.push({
        time: new Date(time).getTime(),
        humidity,
        temperature: temperature_mc / 10,
      });
    }

    const graphData: GraphData = new Array();
    for (const [location, data] of graphMap) {
      graphData.push({ location, data });
    }
    return graphData;
  });
}

export default function Home() {
  const serverGraphData = useRouteData<typeof routeData>();

  return (
    <main class="text-center mx-auto text-gray-700 p-4">
      <h1 class="max-6-xs text-6xl text-sky-700 font-thin uppercase my-16">
        MiiThermostat
      </h1>
      <Show when={serverGraphData()} fallback={<div>Loading...</div>}>
      {(data) => <SensorGraphs data={data()} />}
      </Show>
      <p class="mt-8">
        Visit{" "}
        <a
          href="https://solidjs.com"
          target="_blank"
          class="text-sky-600 hover:underline"
        >
          solidjs.com
        </a>{" "}
        to learn how to build Solid apps.
      </p>
      <p class="my-4">
        <span>Home</span>
        {" - "}
        <A href="/about" class="text-sky-600 hover:underline">
          About Page
        </A>{" "}
      </p>
    </main>
  );
}
