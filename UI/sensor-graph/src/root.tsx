// @refresh reload
import { Suspense } from "solid-js";
import {
  useLocation,
  A,
  Body,
  ErrorBoundary,
  FileRoutes,
  Head,
  Html,
  Meta,
  Routes,
  Scripts,
  Title,
} from "solid-start";
import "./root.css";

export default function Root() {
  const location = useLocation();
  const active = (path: string) =>
    path == location.pathname
      ? "bg-sky-600"
      : "hover:bg-sky-700";
  const header = () => {
    switch(location.pathname) { 
      case "/": { 
         return "Map"; 
      } 
      case "/rooms": { 
         return "Rooms";
      } 
      case "/devices": {
        return "Devices";
      }
      default: { 
        return "";
      } 
   } 
  } 
  return (
    <Html lang="en" class="min-h-full">
      <Head>
        <Title>MiiThermostat</Title>
        <Meta charset="utf-8" />
        <Meta name="viewport" content="width=device-width, initial-scale=1" />
      </Head>
      <Body class="h-full">
        <Suspense>
          <ErrorBoundary>
            <nav class="bg-sky-800">
              <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                <div class="flex h-16 items-center justify-between">
                  <div class="flex items-center">
                    <div class="block">
                      <div class="lg:ml-10 flex items-baseline space-x-4">
                        <A href="/" class={`${active("/")} text-white rounded-md px-3 py-2 text-sm font-medium`} aria-current="page">Map</A>
                        <A href="/rooms" class={`${active("/rooms")} text-white rounded-md px-3 py-2 text-sm font-medium`}>Rooms</A>
                        <A href="/devices" class={`${active("/devices")} text-white rounded-md px-3 py-2 text-sm font-medium`}>Devices</A>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </nav>
            <header class="bg-white shadow">
              <div class="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8 hidden md:block">
                <h1 class="text-3xl font-bold tracking-tight text-gray-900">{header()}</h1>
              </div>
            </header>
            <Routes>
              <FileRoutes />
            </Routes>
          </ErrorBoundary>
        </Suspense>
        <Scripts />
      </Body>
    </Html>
  );
}
