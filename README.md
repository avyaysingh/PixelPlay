
# Video Streaming Platform

A robust backend for a video streaming platform with efficient video transcoding, secure authentication, real-time monitoring, and comprehensive logging for debugging and traceability. Built with **Spring Boot**, **MySQL**, **FFmpeg**, **Prometheus**, and **Grafana**.

## Table of Contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Setup](#setup)
- [Usage](#usage)
- [Monitoring and Metrics](#monitoring-and-metrics)
- [API Documentation](#api-documentation)

---

## Project Overview

This platform serves as a backend service for managing and streaming video content. It offers secure role-based access control, video metadata management, video transcoding, and streaming capabilities. Real-time monitoring and custom metrics collection are integrated to ensure efficient operation and provide system insights.

## Features

- **CRUD Operations**: Manage video metadata through CRUD operations.
- **Video Transcoding**: Utilizes FFmpeg for transcoding videos into streamable formats (HLS).
- **Secure Authentication**: JWT-based authentication and role-based access control protect API endpoints.
- **Streaming**: Supports video streaming with the HLS protocol and byte-range headers to optimize server load.
- **Custom Actuator Endpoints**: Monitor database health and configuration status.
- **Real-Time Monitoring**: Prometheus and Grafana integration using Docker for system monitoring and alerting.
- **Custom Metrics**: API usage metrics tracked using Micrometer for insight into usage patterns.
- **Logging**: Comprehensive logging for key events and debugging.

## Tech Stack

- **Backend**: Java, Spring Boot, FFmpeg
- **Database**: MySQL
- **Monitoring**: Prometheus, Grafana
- **Authentication**: JWT (JSON Web Token)
- **API Documentation**: Swagger

## Getting Started

### Prerequisites

- **Java** 11 or later
- **MySQL** server
- **Docker** and **Docker Compose** (for Prometheus and Grafana)
- **FFmpeg** installed on your system

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/avyaysingh/PixelPlay.git
   cd video-streaming-platform
   ```

2. **Set up MySQL Database**:
   - Create a new MySQL database:
     ```sql
     CREATE DATABASE video_streaming;
     ```
   - Update `application.yml` with your MySQL credentials.

3. **Install FFmpeg** (if not already installed):
   ```bash
   # Ubuntu
   sudo apt update
   sudo apt install ffmpeg

   # macOS (using Homebrew)
   brew install ffmpeg
   ```

4. **Start the Spring Boot application**:
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Run Prometheus and Grafana** (for monitoring) via Docker Compose:
   ```bash
   docker-compose up -d
   ```

## Usage

### Video Transcoding

The platform uses FFmpeg to transcode videos into formats suitable for streaming (HLS). Ensure FFmpeg is accessible from the system path for transcoding to work.

### Authentication

All API endpoints are protected with JWT-based authentication. Users can log in to receive a token, which must be included in the `Authorization` header for subsequent requests.

### API Endpoints

The main API endpoints include:

- **Video Metadata**: `/api/videos` - CRUD operations for video metadata
- **Transcoding and Streaming**: `/api/videos/{id}/stream` - Stream video content
- **Authentication**: `/auth/login`, `/auth/register` - User login and registration

> Detailed API documentation is available via Swagger UI at `http://localhost:8080/swagger-ui.html`.

## Monitoring and Metrics

### Custom Actuator Endpoints

- **Database Health**: Monitors MySQL database connectivity.
- **Configuration Status**: Provides an overview of the system’s configuration properties.

### Prometheus and Grafana

- **Prometheus**: Collects metrics on application performance, accessible at `http://localhost:9090`.
- **Grafana**: Visualizes metrics with customizable dashboards, accessible at `http://localhost:3000`.

### Custom Metrics

Custom metrics track API usage patterns and response times, giving insight into application performance and identifying areas for improvement.

## API Documentation

The full API documentation, including endpoints, request/response formats, and parameter details, is available through Swagger UI:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
