const BASE_URL = process.env.API_URL; // "http://192.168.0.105/api"

function getLastDayMeasurementsAPIURL(): string {
    return `${BASE_URL}/measurements/lastday`;
}

function getRoomsAPIURL(): string {
    return `${BASE_URL}/locations/`
}

function getDevicesAPIURL(): string {
    return `${BASE_URL}/devices/`
}

export {getLastDayMeasurementsAPIURL, getRoomsAPIURL, getDevicesAPIURL}