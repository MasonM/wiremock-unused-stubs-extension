#!/bin/bash

PORT=5081

reset() {
	curl -s -X POST http://localhost:$PORT/__admin/requests/reset > /dev/null
	curl -s -X DELETE http://localhost:$PORT/__admin/mappings > /dev/null
	curl -s -d '{ "request": { "method": "GET", "url": "/foo" }, "response": { "body": "FOO" } }' http://localhost:$PORT/__admin/mappings > /dev/null
	curl -s -d '{ "request": { "method": "GET", "url": "/bar" }, "response": { "body": "FOO" } }' http://localhost:$PORT/__admin/mappings > /dev/null
}

results() {
	echo -e "UNUSED:"; curl http://localhost:$PORT/__admin/unused_mappings
	curl -s -X DELETE http://localhost:$PORT/__admin/unused_mappings
	echo -e "\n\nAFTER DELETE:"; curl http://localhost:$PORT/__admin/mappings
	reset
}


echo -e "Trying with no requests (should delete both):"
results

echo -e "\n\nTrying with one matching request (should delete bar):"
curl -s http://localhost:$PORT/foo > /dev/null
curl -s http://localhost:$PORT/baz > /dev/null
results

echo -e "\n\nTrying with both requests matching (should not delete any):"

curl -s http://localhost:$PORT/foo > /dev/null
curl -s http://localhost:$PORT/baz > /dev/null
curl -s http://localhost:$PORT/bar > /dev/null
results
