# Postman Demo Checklist

Use this guide to validate the backend APIs in sequence.

## 0. Prerequisites
- Database initialized with docs/DATABASE.sql
- Backend running: mvn spring-boot:run
- If app.auth.token is set, include header X-API-KEY

Base URL: http://localhost:8080

## 1. Create User
Method: POST
URL: /api/users
Headers:
- Content-Type: application/json

Body:
{
  "studentNo": "S2026001",
  "name": "Test User",
  "phone": "13800000000",
  "avatarUrl": "https://example.com/avatar.png"
}

Expected:
- 200 OK
- Response contains id

## 2. Create Product
Method: POST
URL: /api/products
Headers:
- Content-Type: application/json

Body:
{
  "name": "Eco Bottle",
  "description": "Reusable bottle",
  "pricePoints": 20,
  "stock": 50,
  "imageUrl": "https://example.com/bottle.png"
}

Expected:
- 200 OK
- Response contains id

## 3. AI Verify (Image Upload)
Method: POST
URL: /api/ai/verify
Form-Data:
- userId: <USER_ID>
- behaviorType: clean_plate
- imageUrl: https://example.com/photo.jpg
- file: (binary image)

Expected:
- 200 OK
- Response has decision, score, threshold, points

## 4. Behavior List
Method: GET
URL: /api/behaviors?userId=<USER_ID>&page=0&size=10

Expected:
- 200 OK
- List contains latest behavior record

## 5. Exchange Create
Method: POST
URL: /api/exchanges
Headers:
- Content-Type: application/json

Body:
{
  "userId": <USER_ID>,
  "productId": <PRODUCT_ID>,
  "amount": 1
}

Expected:
- 200 OK
- Response includes redeemCode

## 6. Exchange Redeem
Method: POST
URL: /api/exchanges/redeem
Headers:
- Content-Type: application/json

Body:
{
  "redeemCode": "<REDEEM_CODE>"
}

Expected:
- 200 OK
- redeemStatus updated

## 7. Exchange List
Method: GET
URL: /api/exchanges?userId=<USER_ID>&page=0&size=10

Expected:
- 200 OK
- List contains exchange record

## 8. Error Response Check
Method: POST
URL: /api/users
Headers:
- Content-Type: application/json

Body:
{
  "studentNo": "",
  "name": ""
}

Expected:
- 400
- ApiErrorResponse with code VALIDATION_ERROR

## 9. Auth Check (If Enabled)
Set app.auth.token in application.yml to a non-empty string.

Request header:
- X-API-KEY: <TOKEN>

Expected:
- Without header: 401
- With header: 200
