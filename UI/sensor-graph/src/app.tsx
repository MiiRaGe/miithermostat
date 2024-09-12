// @refresh reload
import { Suspense, ErrorBoundary } from "solid-js";
import "./app.css";
import { FileRoutes } from "@solidjs/start/router";
import { Router, } from "@solidjs/router";
import { Meta, MetaProvider, Title } from "@solidjs/meta";
import Nav from "~/components/Nav";

export default function Root() {

  return (
    <div class="min-h-full">
      <MetaProvider>
        <Title>MiiThermostat</Title>
        <Meta charset="utf-8" />
        <Meta name="viewport" content="width=device-width, initial-scale=1" />
      </MetaProvider>
      <div class="h-full w-full">
        <ErrorBoundary fallback={(err, reset) => <div onClick={reset}>Error: {err.toString()}</div>}>
          <div>
            <Router root={props => (
              <>
              <Nav />
              <Suspense>{props.children}</Suspense>
              </>)}>          
              <FileRoutes />
            </Router>
          </div>
        </ErrorBoundary>
      </div>
    </div>
  );
}
