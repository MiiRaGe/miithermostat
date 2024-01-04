/// <reference types="solid-start/env" />


declare type Device = { id: string, location: string|null }
declare type Devices = Array<Device>

declare type Room = { name: string, devices: Devices, data: { time: number, humidity_pt: number, temperature_mc: number } }
declare type Rooms = Array<Room>

declare type Measurement = { location: string, time: number, humidity_pt: number, temperature_mc: number }
declare type Measurements = Array<Measurement>

declare type GraphData = Array<{ location: string, data: Array<{ humidity: number, temperature: number, time: number }> }>

declare type Assignements = { unassignedDevices: Devices, locations: Rooms }