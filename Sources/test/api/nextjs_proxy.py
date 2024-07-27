#!/usr/bin/env python

# Python test script

import requests

# return textual response
def request(url, body):
    response = requests.post(url, json=body)
    return response.text


nextjs_url = "http://localhost:3000"
java_url = "http://localhost:8000"

# test nextjs
nextjs_response = request(nextjs_url + "/api/sim/instructionDescription", {})
java_response = request(java_url + "/instructionDescription", {})

print("NextJS response: " + nextjs_response)
print("Java response: " + java_response)

print("NextJS response == Java response: " + str(nextjs_response == java_response))

