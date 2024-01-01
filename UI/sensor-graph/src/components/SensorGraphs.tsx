import { onMount } from 'solid-js'
import { For } from "solid-js";
import { Chart, Title, Tooltip, Legend, Colors, TimeScale } from 'chart.js'
import { Line } from 'solid-chartjs'
import 'chartjs-adapter-luxon'

const SensorGraphs = (props: { data: GraphData | undefined }) => {
  if (props.data == undefined) {
    return <div>Missing Graph Data</div>
  }
  /**
   * You must register optional elements before using the chart,
   * otherwise you will have the most primitive UI
   */
  onMount(() => {
    Chart.register(Title, Tooltip, Legend, Colors, TimeScale)
  })

  const chartDataObjs = [];
  for (const { location, data } of props.data) {
    const chartDataObj = {
      chartData: {
        labels: data.map(row => row.time),
        datasets: [{
          label: "Humidity",
          data: data.map(row => row.humidity),
        },
        {
          label: "Temperature",
          data: data.map(row => row.temperature),
        },]
      },
      chartOptions: {
        spanGaps: 120000,
        responsive: true,
        interaction: {
          mode: 'nearest',
        },
        elements: {
          point: {
            radius: 0,
            hitRadius: 8,
            hoverRadius: 4,
          }
        },
        plugins: {
          title: {
            display: true,
            text: location,
            position: 'top',
          },
          decimation: {
            enabled: true,
          }
        },
        scales: {
          x: {
            type: 'time',
            time: {
              tooltipFormat: 'DD T',
            },
            ticks: {
              autoSkip: false,
              maxRotation: 0,
              major: {
                enabled: true,
              },
            },
          },
          y: {
            suggestedMin: 0,
            suggestedMax: 60,
          }
        },
        maintainAspectRatio: false,
      }
    }
    chartDataObjs.push(chartDataObj)
  }

  return (
    <div>
      <For each={chartDataObjs}>
        {(chartDataObj) =>
          <div class="min-h-min max-h-52 min-w-min max-w-full ml-5 mr-5 mb-5 shadow p-2">
            <Line
              data={chartDataObj.chartData}
              options={chartDataObj.chartOptions}
              width={300}
              height={200} />
          </div>
        }
      </For>
    </div>
  )
}

export { SensorGraphs }