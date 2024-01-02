import { Show } from "solid-js";
import { SensorGraphs } from "~/components/SensorGraphs";
import { useRouteData } from "solid-start";
import { createServerData$ } from "solid-start/server";
import { getLastDayMeasurementsAPIURL } from "~/API/api";

export function routeData() {
  return createServerData$(async () => {
    const response = await fetch(await getLastDayMeasurementsAPIURL());
    let measurements = await response.json() as Measurements;

    const graphMap: Map<string, Array<{ time: number, humidity: number, temperature: number }>> = new Map();
    for (const { location, time, humidity_pt, temperature_mc } of measurements) {
      if (graphMap.get(location) == undefined) graphMap.set(location, new Array());
      graphMap.get(location)?.push({
        time: new Date(time).getTime(),
        humidity: humidity_pt / 10,
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
    <main class="mx-auto max-w-7xl py-6 sm:px-6 lg:px-8">
      <Show when={serverGraphData()} fallback={<div>Loading...</div>}>
      {(data) => <SensorGraphs data={data()} />}
      </Show>
    </main>
  );
}
