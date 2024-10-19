"use server";

const getLastDayMeasurements = async () => {
    const response = await fetch(`${process.env.API_URL}/measurements/lastday`);
    return await response.json() as Measurements;
};

export {getLastDayMeasurements}