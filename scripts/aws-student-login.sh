#!/usr/bin/env bash
set -euo pipefail

PROFILE="aws-student"
REGION="us-east-1"

AWS_ACCESS_KEY_ID="ASIARZKWKXECUW24XFVD"
AWS_SECRET_ACCESS_KEY="whPrTT7zOanDOq4zjx8h3rBWsynnB0Fbup4qfSxJ"
AWS_SESSION_TOKEN="IQoJb3JpZ2luX2VjEAkaCXVzLXdlc3QtMiJHMEUCIFgoD9n222U0xxkgOsVy1P35Oj3xVUxv0twg+2VaceFnAiEAleUY7FzhYPQegyhFIQITNtpQurRkfG+FYDlzmak1Oc4qugII0v//////////ARABGgwxMjMxMjQ0OTY2NDUiDE1PSKdQ1jC52QZLWiqOAoEn/YPmKYRvkb/CzQ17eaxG7XE44SXevJOnf4zsb5Twrj2oOIRx7fFivhYVfOIiTQ4YjePYbcKtcCOBcG4F8LABeI0ms/b/zWjIY4WoVvH0JH/fLRlwwpC3UwF+uFmRPVdD25LGv7Vhs7J0iMqLW75WQrlySWVaHjmrzs0CYvBwOzCdCkQAgFvhvVCY8CI445cfdvMD3p4cKUz2nvqWRxd1KklcfPi68ScuPXetePa+89GcxyHmpOEbDeVbxfr4byp/67+RRgtLZ7NSki4Lm7DNeT5TUJG5qlIxGW/tfeSR2gzvbskl3PS54fN2fZIeZ2vGLJy8NvDTZ1/06Lrj2qidb+tLAk1tjU3MsSx5/jDnwZHSBjqdASgRTEQwpozY0sWm4udjDoMJ+aOsLknT6khRvlMgMMgxv3OS0L4ivBr+7wAGLtdjhM9kzhIxlEqqgQKb2wrqbwgKyu8v12fAN/p8Gjzict56K+HbNuiRjjpUUzYIuRYE2zcx8/jZQq0Ma8cJnvbjXl/QreXipfxFGR6L0gO8/P6Et2GH9dkEKNf1qVCJ65EpGTgxIwPT+Rw6QePQx4Q="

aws configure set aws_access_key_id "$AWS_ACCESS_KEY_ID" --profile "$PROFILE"
aws configure set aws_secret_access_key "$AWS_SECRET_ACCESS_KEY" --profile "$PROFILE"
aws configure set aws_session_token "$AWS_SESSION_TOKEN" --profile "$PROFILE"
aws configure set region "$REGION" --profile "$PROFILE"
aws configure set output json --profile "$PROFILE"

echo "Validando credenciais..."

aws sts get-caller-identity --profile "$PROFILE"

echo
echo "Profile '$PROFILE' configurado com sucesso."