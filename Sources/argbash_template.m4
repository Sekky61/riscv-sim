#!/bin/bash
# m4_ignore(
echo "This is just a script template, not the script (yet) - pass it to 'argbash' to fix this." >&2
exit 11  #)Created by argbash-init v2.10.0
# ARG_OPTIONAL_SINGLE([internal-api],[],[Internal API prefix],[simserver:8000])
# ARG_OPTIONAL_SINGLE([external-api],[],[External API prefix],[/api/sim])
# ARG_OPTIONAL_SINGLE([http-port],[],[HTTP port],[3120])
# ARG_OPTIONAL_SINGLE([https-port],[],[HTTPS port],[3121])
# ARG_OPTIONAL_SINGLE([ssl-conf],[],[Path to SSL configuration],[])
# ARG_OPTIONAL_SINGLE([certs-path],[],[Path to SSL certificates],[])
# ARG_OPTIONAL_SINGLE([build-strategy],[],[Build strategy (pull/build)],[pull])
# ARG_OPTIONAL_SINGLE([compose-command],[],[Docker compose command (docker compose/docker-compose)],[docker compose])
# ARG_POSITIONAL_SINGLE([command],[Command to execute (up/down/status/logs)],[])
# ARG_HELP([Management script for RISC-V Simulator Docker environment])
# ARG_VERSION([echo $0 v0.1])
# ARGBASH_GO


# To compile it, run
# argbash argbash_template.m4 -o manage-riscvsim.sh

# [ <-- needed because of Argbash

# Print error message
error() {
    echo -e "Error: $1" >&2
    exit 1
}

# Print success message
success() {
    echo -e "$1"
}

# Print warning message
warning() {
    echo -e "Warning: $1"
}

# Validate ports
validate_port() {
    if ! [[ $1 =~ ^[0-9]+$ ]] || [ $1 -lt 1 ] || [ $1 -gt 65535 ]; then
        error "Invalid port number: $1"
    fi
}

# Validate build strategy
validate_build_strategy() {
    if [[ "$1" != "pull" && "$1" != "build" ]]; then
        error "Build strategy must be either 'pull' or 'build'"
    fi
}

# Validate docker compose command
validate_docker_compose() {
    if [[ "$1" != "docker compose" && "$1" != "docker-compose" ]]; then
        error "Docker compose command must be either 'docker compose' or 'docker-compose'"
    fi
}

# Check if required files exist
check_requirements() {
    if [ ! -f "docker-compose.yml" ]; then
        error "docker-compose.yml not found in current directory"
    fi

    if [ "$_arg_command" = "up" ]; then
        if [ ! -f "$_arg_ssl_conf" ]; then
            warning "SSL configuration file not found at $_arg_ssl_conf"
        fi
        if [ ! -d "$_arg_certs_path" ]; then
            warning "Certificates directory not found at $_arg_certs_path"
        fi
    fi
}

# Export environment variables for docker-compose
export_variables() {
    export INTERNAL_SIM_API_PREFIX="$_arg_internal_api"
    export EXTERNAL_SIM_API_PREFIX="$_arg_external_api"
    export HTTP_PORT="$_arg_http_port"
    export HTTPS_PORT="$_arg_https_port"
    export SSL_CONF_PATH="$_arg_ssl_conf"
    export CERTS_PATH="$_arg_certs_path"
}

# Execute docker-compose commands
execute_command() {
    compose=$_arg_compose_command
    case $_arg_command in
        up)
            if [ "$_arg_build_strategy" = "build" ]; then
                success "Building and starting services..."
                $compose up --build -d
            else
                success "Pulling images and starting services..."
                $compose pull
                $compose up -d
            fi
            ;;
        down)
            success "Stopping services..."
            $compose down
            ;;
        status)
            success "Checking service status..."
            $compose ps
            ;;
        logs)
            success "Showing logs..."
            $command logs --tail=100 -f
            ;;
        *)
            error "Invalid command: $_arg_command. Must be one of: up, down, status, logs"
            ;;
    esac
}

# Main script execution
main() {
    validate_port "$_arg_http_port"
    validate_port "$_arg_https_port"
    validate_build_strategy "$_arg_build_strategy"
    validate_docker_compose "$_arg_compose_command"
    check_requirements
    export_variables
    execute_command
}

main

# ] <-- needed because of Argbash

