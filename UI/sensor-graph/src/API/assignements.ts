"use server";

const getAssignements = async () => {
    const response = await fetch(`${process.env.API_URL}/assignements/`);
    return await response.json() as Assignements;
};

const saveAssignements = async (assignements: Assignements) => {
    const response = await fetch(`${process.env.API_URL}/assignements/`, {
        method: 'POST',
        body: JSON.stringify(assignements),
        headers: { 'Content-Type': 'application/json' }
    });
    
    return {ok: response.ok, text: await response.text(), status: response.status};
}

export { getAssignements, saveAssignements }