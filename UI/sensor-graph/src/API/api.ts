"use server"

const serverGetBaseUrl = () => {
    return process.env.API_URL;
}

const getLastDayMeasurementsAPIURL = () => {
    return `${serverGetBaseUrl()}/measurements/lastday`;
}

const getRoomsAPIURL = (id?: String) =>{
    const baseApi = serverGetBaseUrl()
    let url = `${baseApi}/locations/`
    if (id != null) {
        url += `${id}/`
    }
    return url
}

const getAssignementsAPIURL =  () => {
    return `${serverGetBaseUrl()}/assignements/`
}

const getLastDayMeasurements = async () => {
    const response = await fetch(getLastDayMeasurementsAPIURL());
    return await response.json() as Assignements
};

const getRooms = async () => {
    const response = await fetch(getRoomsAPIURL());
    return await response.json() as Rooms;
};


const getAssignements = async () => {
    const response = await fetch(getAssignementsAPIURL());
    return await response.json() as Measurements;
};

const createRoom = async (name: String) => {
    const response = await fetch(getRoomsAPIURL(), {
        method: 'POST',
        body: JSON.stringify({name}),
        headers: {'Content-Type': 'application/json'}
    });
    return {ok: response.ok, text: response.text()}
};

const deleteRoom = async (name: String) => {
    const response = await fetch(getRoomsAPIURL(name),  {
        method: 'DELETE',
      });
    return response.ok
};


export {getLastDayMeasurements, getRooms, getAssignements, createRoom, deleteRoom}