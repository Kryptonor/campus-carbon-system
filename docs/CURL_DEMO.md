# cURL Demo Checklist

Base URL: http://localhost:8080
If app.auth.token is set, add header: X-API-KEY: <TOKEN>

## 1. Create User
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d "{\"studentNo\":\"S2026001\",\"name\":\"Test User\",\"phone\":\"13800000000\",\"avatarUrl\":\"https://example.com/avatar.png\"}"

## 2. Create Product
curl -X POST "http://localhost:8080/api/products" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Eco Bottle\",\"description\":\"Reusable bottle\",\"pricePoints\":20,\"stock\":50,\"imageUrl\":\"https://example.com/bottle.png\"}"

## 3. AI Verify (Image Upload)
# Replace <USER_ID> and /path/to/photo.jpg
curl -X POST "http://localhost:8080/api/ai/verify" \
  -F "userId=<USER_ID>" \
  -F "behaviorType=clean_plate" \
  -F "imageUrl=https://example.com/photo.jpg" \
  -F "file=@/path/to/photo.jpg"

## 4. Behavior List
curl -X GET "http://localhost:8080/api/behaviors?userId=<USER_ID>&page=0&size=10"

## 5. Exchange Create
# Replace <USER_ID> and <PRODUCT_ID>
curl -X POST "http://localhost:8080/api/exchanges" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":<USER_ID>,\"productId\":<PRODUCT_ID>,\"amount\":1}"

## 6. Exchange Redeem
# Replace <REDEEM_CODE>
curl -X POST "http://localhost:8080/api/exchanges/redeem" \
  -H "Content-Type: application/json" \
  -d "{\"redeemCode\":\"<REDEEM_CODE>\"}"

## 7. Exchange List
curl -X GET "http://localhost:8080/api/exchanges?userId=<USER_ID>&page=0&size=10"

## 8. Error Response Check
curl -X POST "http://localhost:8080/api/users" \
  -H "Content-Type: application/json" \
  -d "{\"studentNo\":\"\",\"name\":\"\"}"

## 9. Auth Check (If Enabled)
# With API key
curl -X GET "http://localhost:8080/api/products" \
  -H "X-API-KEY: <TOKEN>"
