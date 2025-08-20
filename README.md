# Online Store API

## Table of Contents
- [Description](#description)
- [Installation](#installation)
- [Requirements](#requirements)
- [Features](#features)
- [Notes](#notes)

## Description

**Online Store API** is a **Java Spring Boot backend** for an e-commerce platform, designed to manage **categories, products, stock, orders, order history, and payments** with a **HttpOnly cookie-based authentication system**.  

**Project Goals and Motivation:**  
- To create a substantial project that strengthens my resume and serves as a practical way to deepen my knowledge in **Java Spring Boot backend development**.  
- To gain hands-on experience working on a larger project, understanding **key backend concepts** such as **asynchronous processing** and **caching**.  

## Installation

- Clone the repository : ```bash git clone https://github.com/rolinagy0513/online-store-api.git
- Rename the application.yml.example to application.yml and fill in the required values.
- Build the project: mvn clean install
- Run the project: mvn spring-boot:run
- The API will be available at: http://localhost:8080/ by default.

## Requirements

- Java 21 or higher  
- PostgreSQL database  
- Maven  
- IDE or code editor (IntelliJ, Eclipse, etc.)  
- [Stripe CLI](https://stripe.com/docs/stripe-cli) for payment webhook testing

## Features

- HttpOnly cookie-based authentication system with multiple roles: USER, ADMIN, MANAGER.
- CRUD operations for categories and products: getAll and getOne endpoints are optimized with async and caching, add/edit/delete operations are restricted to admins only.
- Add to Cart functionality: users can add products to their cart. Product stock is tracked with a dedicated Stock entity.
- Stripe integration for payments: Creates a unique payment session for each order in sandbox mode. Handles payment webhooks to update order history and stock:
    - On success: products are removed from stock, order added to user history.
    - On failure: stock is restored.
- Stripe CLI integration allows development testing: ```bash stripe listen --forward-to http://localhost:8080/api/webhook/stripe
- Unit tests for all services.
- Comprehensive documentation for most files and methods.

## Notes

- The authentication system is based on open-source solutions with some custom modifications.
- The project is still under development. Future enhancements include:
    - Adding a Docker configuration for easier development and deployment.
    - Expanding features and improving code quality as my knowledge grows.
