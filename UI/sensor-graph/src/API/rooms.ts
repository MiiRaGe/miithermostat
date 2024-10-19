"use server";

const getRoomsAPIURL = async (id?: String) =>{
    const baseApi = `${process.env.API_URL}`
    let url = `${baseApi}/locations/`
    if (id != null) {
        url += `${id}/`
    }
    return url
}

const getRooms = async () => {
    const response = await fetch(await getRoomsAPIURL());
    return await response.json() as Rooms;
};

const createRoom = async (name: String) => {
    const response = await fetch(await getRoomsAPIURL(), {
        method: 'POST',
        body: JSON.stringify({name}),
        headers: {'Content-Type': 'application/json'}
    });
    return {ok: response.ok, text: await response.text()}
};

const deleteRoom = async (name: String) => {
    const response = await fetch(await getRoomsAPIURL(name),  {
        method: 'DELETE',
      });
    return response.ok
};

export {getRooms, createRoom, deleteRoom}