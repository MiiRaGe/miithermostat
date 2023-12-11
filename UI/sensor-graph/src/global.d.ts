/// <reference types="solid-start/env" />


declare type Device = { id: string }
declare type Devices = Array<Device>

declare type Room = { name: string, devices: Devices }
declare type Rooms = Array<Room>

declare type Measurement = { location: string, time: number, humidity: number, temperature_mc: number }
declare type Measurements = Array<Measurement>

declare type GraphData = Array<{ location: string, data: Array<{ humidity: number, temperature: number, time: number }> }>
