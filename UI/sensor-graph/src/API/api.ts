'use server';

import server$ from "solid-start/server";

const getBaseUrl = async () => {
    return process.env.API_URL
}

const serverGetBaseUrl = server$(getBaseUrl)

const getLastDayMeasurementsAPIURL = async () => {
    return `${await serverGetBaseUrl()}/measurements/lastday`;
}

const getRoomsAPIURL = async () =>{
    return `${await serverGetBaseUrl()}/locations/`
}

const getDevicesAPIURL = async () => {
    return `${await serverGetBaseUrl()}/devices/`
}

export {getLastDayMeasurementsAPIURL, getRoomsAPIURL, getDevicesAPIURL}