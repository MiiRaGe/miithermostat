"use server";

import {GET} from "@solidjs/start";

const serverGetBaseUrl = GET(async () => {
    return process.env.API_URL;
});


export {serverGetBaseUrl}