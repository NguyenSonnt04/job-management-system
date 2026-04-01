# Chatbot Claude AI Integration Guide

## Overview
Project đã tích hợp **Claude AI** (Anthropic) **chỉ cho chatbot**, trong khi các tính năng CV khác vẫn sử dụng **Gemini AI**.

## Architecture

### Services sử dụng AI:

| Service | AI Provider | Purpose |
|---------|-------------|---------|
| **ContactChatbotService** | ✅ Claude AI | Chatbot trợ lý ảo |
| **CvParseController** | Gemini AI | Parse CV, trích xuất thông tin |
| **CvScoringController** | Gemini AI | Chấm điểm CV |
| **CvScoringController** | Gemini AI | Match CV với jobs |

### Đã tạo mới:
- ✅ `ClaudeChatbotService.java` - Service riêng cho chatbot với Claude
- ✅ `pom.xml` - Thêm Anthropic Claude SDK dependency
- ✅ `application.properties` - Thêm claude.api.key

### Không thay đổi:
- ❌ `GeminiService.java` - Giữ nguyên cho CV features
- ❌ `CvParseController.java` - Vẫn dùng Gemini
- ❌ `CvScoringController.java` - Vẫn dùng Gemini

## Cấu hình

### Bước 1: Lấy API Keys

**Gemini API Key (bắt buộc):**
1. Truy cập: https://makersuite.google.com/app/apikey
2. Đăng nhập bằng Google Account
3. Tạo API key mới
4. Copy API key

**Claude API Key (bắt buộc):**
1. Truy cập: https://console.anthropic.com/
2. Đăng ký hoặc đăng nhập
3. Vào phần API Keys
4. Tạo API key mới
5. Copy API key

### Bước 2: Cấu hình Application Properties

Tạo file `src/main/resources/application-local.properties`:

```properties
# Gemini API - cho CV features
gemini.api.key=AIzaSyYourGeminiApiKeyHere

# Claude API - cho chatbot
claude.api.key=sk-ant-api03-your-claude-api-key-here
```

Hoặc sử dụng environment variables:

```bash
# Linux/Mac
export GEMINI_API_KEY=AIzaSyYourGeminiApiKeyHere
export CLAUDE_API_KEY=sk-ant-api03-your-claude-api-key-here

# Windows
set GEMINI_API_KEY=AIzaSyYourGeminiApiKeyHere
set CLAUDE_API_KEY=sk-ant-api03-your-claude-api-key-here
```

### Bước 3: Build và Run

```bash
mvn clean install
mvn spring-boot:run
```

## Chatbot Features với Claude AI

### Ưu điểm của Claude cho Chatbot:
- ✅ **Trả lời tự nhiên hơn** - Claude có khả năng hội thoại tốt hơn
- ✅ **Hiểu context tốt hơn** - Giữ được context trong cuộc hội thoại
- ✅ **Ngôn ngữ tự nhiên** - Trả lời thân thiện, gần gũi
- ✅ **Reasoning tốt hơn** - Phân tích và gợi ý việc làm chính xác hơn

### Chatbot System Prompt:
```
Bạn là trợ lý AI chuyên nghiệp cho JCO.

VAI TRÒ CỦA BẠN:
- Hỗ trợ người tìm việc: tìm kiếm công việc, viết CV, ứng tuyển
- Hỗ trợ nhà tuyển dụng: đăng tin, tìm ứng viên
- Gợi ý việc làm phù hợp dựa trên kỹ năng, kinh nghiệm

NGUYÊN TẮC TRẢ LỜI:
- Trả lời ngắn gọn, thân thiện (dưới 100 từ)
- LUÔN xưng là "mình" và gọi người dùng là "bạn"
- Dùng ngôn ngữ tự nhiên, thân thiện
```

## Kiểm tra integration

### Test Chatbot:
1. Mở application
2. Đi đến trang chatbot
3. Đặt câu hỏi: "Xin chào, mình là Java developer, hãy gợi ý việc làm phù hợp"
4. Claude sẽ trả lời với gợi ý việc làm từ database

### Test CV Features (vẫn dùng Gemini):
- ✅ Parse CV: `POST /api/cv/parse-profile`
- ✅ Score CV: `POST /api/cv-scoring/score`
- ✅ Match Jobs: `POST /api/cv-scoring/match-jobs`

## Troubleshooting

### Lỗi: "Claude API key chưa được cấu hình"
**Solution:**
```properties
# Trong application-local.properties
claude.api.key=sk-ant-api03-your-actual-key
```

### Lỗi: "Gemini API key chưa được cấu hình"
**Solution:**
```properties
# Trong application-local.properties
gemini.api.key=AIzaSyYourActualGeminiKey
```

### Chatbot không hoạt động nhưng CV features bình thường
**Nguyên nhân:** Thiếu Claude API key
**Solution:** Thêm claude.api.key vào application-local.properties

### CV features không hoạt động
**Nguyên nhân:** Thiếu Gemini API key
**Solution:** Thêm gemini.api.key vào application-local.properties

## Chi phí

### Gemini API (Google AI):
- **Miễn phí:** 60 requests/phút
- **Giá:** $0.07-0.30 per million tokens
- **Dùng cho:** CV parsing, scoring, job matching

### Claude API (Anthropic):
- **Model:** claude-3-5-sonnet-20241022
- **Giá:** ~$3-15 per million tokens
- **Dùng cho:** Chatbot only

## Migration Notes

### Tại sao chỉ chatbot dùng Claude?
1. **Chatbot cần hội thoại tự nhiên** - Claude giỏi hơn về conversation
2. **CV features cần cấu trúc JSON** - Gemini đã ổn định cho JSON output
3. **Chi phí tối ưu** - Claude đắt hơn, chỉ dùng nơi cần thiết
4. **Risk minimization** - Nếu có vấn đề, chỉ ảnh hưởng chatbot

### Quyết định architecture:
- ✅ Hybrid approach: Gemini + Claude
- ✅ Best of both worlds
- ✅ Cost-effective
- ✅ Easy to maintain

## Hỗ trợ

- **Claude Documentation:** https://docs.anthropic.com/
- **Gemini Documentation:** https://ai.google.dev/docs
- **Project Issues:** Contact maintainers
