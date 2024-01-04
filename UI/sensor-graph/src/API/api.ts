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

const getAssignementsAPIURL = async () => {
    return `${await serverGetBaseUrl()}/assignements/`
}

export {getLastDayMeasurementsAPIURL, getRoomsAPIURL, getDevicesAPIURL, getAssignementsAPIURL}