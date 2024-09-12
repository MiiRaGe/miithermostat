import { useLocation, A } from "@solidjs/router";

export default function Nav() {
    const location = useLocation();
    const active = (path: string) =>
        path == location.pathname
            ? "bg-sky-600"
            : "hover:bg-sky-700";
    const header = () => {
        switch (location.pathname) {
            case "/": {
                return "Map";
            }
            case "/rooms": {
                return "Rooms";
            }
            case "/assignements": {
                return "Device and Location assignements";
            }
            default: {
                return "";
            }
        }
    };

    return (
        <>
            <nav class="bg-sky-800">
                <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div class="flex h-16 items-center justify-between">
                        <div class="flex items-center">
                            <div class="block">
                                <div class="lg:ml-10 flex items-baseline space-x-4">
                                    <A href="/" class={`${active("/")} text-white rounded-md px-3 py-2 text-sm font-medium`} aria-current="page">Map</A>
                                    <a href="/rooms" class={`${active("/rooms")} text-white rounded-md px-3 py-2 text-sm font-medium`}>Rooms</a>
                                    <a href="/assignements" class={`${active("/assignements")} text-white rounded-md px-3 py-2 text-sm font-medium`}>Assignements</a>
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
        </>
    );
}
