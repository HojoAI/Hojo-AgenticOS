# hojoai-parent — API Documentation

## Overview

`hojoai-parent` is a **Maven parent POM (BOM)**, used for unified dependency versions and plugin configuration, **does not contain any runnable application and provides no network APIs**.

## Description

- Does not participate in runtime deployment; no HTTP, gRPC, or WebSocket endpoints.
- Child projects inherit this POM to get unified Spring Boot, Spring Cloud, gRPC, and other version management.