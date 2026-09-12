# 🎓 AI-Powered Exam Paper Generator

A sophisticated full-stack Spring Boot application that utilizes **RAG (Retrieval-Augmented Generation)** to transform personal PDF notes into structured school exam papers. This project features a distributed architecture, offloading heavy AI computation to a dedicated local server.

---

## 🧠 System Architecture & Workflow
The project is built on the principle of **Retrieval-Augmented Generation (RAG)**. Instead of relying on a model's general knowledge, it "retrieves" your specific notes to "augment" the AI's generation.

### 1. Ingestion (The Library)
* **PDF Processing:** Documents are parsed and broken into smaller text "chunks".
* **Vector Embeddings:** Text is converted into mathematical vectors using the `nomic-embed-text` model on a remote Windows PC.
* **Vector Storage:** These vectors are stored in **Pinecone**, organized by Subject and Class metadata for precise retrieval.

### 2. Retrieval & Generation (The Brain)
* **Similarity Search:** When an exam is requested, the system finds the most relevant chunks in Pinecone based on the chapter name.
* **Contextual Prompting:** The retrieved text is injected into a "Teacher Prompt" sent to **Llama 3.1**.
* **Execution:** The LLM generates questions (MCQs, Long Answers) and an Answer Key based *only* on the provided notes.

### 3. Storage & Export (The Archive)
* **MongoDB Atlas:** Every generated paper is saved to a cloud-hosted history log.
* **Professional PDF:** Uses **OpenPDF** to generate branded A4 documents with automated page breaks for the answer key.

---

## 🛠️ Tech Stack
| Component | Technology |
| :--- | :--- |
| **Framework** | Spring Boot 3.x, Spring AI |
| **AI Models** | Llama 3.1 (LLM), nomic-embed-text (Embeddings) |
| **Vector DB** | Pinecone |
| **Database** | MongoDB Atlas |
| **Cache / Queue** | Upstash Redis |
| **Compute / Hosting** | AWS Lambda (Container Image) with AWS Lambda Web Adapter |
| **Networking** | Tailscale (Secure Mesh VPN) |
| **Frontend** | Thymeleaf, Bootstrap 5 |
| **PDF Engine** | OpenPDF / LibrePDF |

---

## 🚀 Key Features
* **Distributed AI:** Seamlessly connects a MacBook client to a Windows AI server over any network via **Tailscale**.
* **Smart Subject Silos:** Metadata filtering ensures Physics questions only come from Physics notes.
* **Automated Answer Keys:** AI places answers at the end of the document after a strict delimiter.
* **Branded PDF Export:** Server-side PDF generation ensures consistent formatting with school logos.
* **Zero Idle Cost Architecture:** Hosted on AWS Lambda with a Function URL, scaling down to 0 instances when not in use.

---

## 📂 API Summary
* `GET /exams` - Access the main generator dashboard.
* `POST /api/exams/generate` - Trigger the AI pipeline and save to MongoDB.
* `GET /exams/history/results` - Search the Atlas database for past papers.
* `GET /api/pdf/download/{id}` - Generate and download the official PDF.

---

## 🌐 Live Access
* **Live Endpoint:** `https://2t2saofkd5ngmt7ahmrnaynujq0tutlw.lambda-url.ap-south-1.on.aws/exams`
* **Shorter URL: ** `https://bit.ly/papergenerator`

---

## 🐳 Docker Build & Deploy to AWS Lambda

Whenever you make code updates or enhancements, run this sequence to build the single-architecture container, push to Amazon ECR, and update the live Lambda function.

### 1. Authenticate Docker with ECR
```bash
aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin 558260070804.dkr.ecr.ap-south-1.amazonaws.com
```

### 2. Build Multi-Stage Image (Provenance Disabled)
> **Note:** `--provenance=false` is strictly required to ensure Docker generates a single-manifest OCI image compatible with AWS Lambda.
```bash
docker buildx build \
  --platform linux/amd64 \
  --provenance=false \
  -t 558260070804.dkr.ecr.ap-south-1.amazonaws.com/paper-generation:latest \
  --load .
```

### 3. Push Image to ECR
```bash
docker push 558260070804.dkr.ecr.ap-south-1.amazonaws.com/paper-generation:latest
```

### 4. Deploy New Image to Lambda
> Simply pushing to ECR does not automatically reload Lambda. Run this command to tell Lambda to pull the new `:latest` digest:
```bash
aws lambda update-function-code \
  --function-name paper-generation-app \
  --image-uri 558260070804.dkr.ecr.ap-south-1.amazonaws.com/paper-generation:latest \
  --region ap-south-1
```

### 5. Tail CloudWatch Logs (Optional Debugging)
```bash
aws logs tail /aws/lambda/paper-generation-app --region ap-south-1 --follow
```

---

## ⚡ Automated Deployment Script (`deploy.sh`)

You can create an executable script in the root of your project to automate deployments:

```bash
#!/usr/bin/env bash
set -e

ACCOUNT_ID="558260070804"
REGION="ap-south-1"
REPO="paper-generation"
FUNCTION="paper-generation-app"
IMAGE_URI="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/${REPO}:latest"

echo "🔐 Logging in to Amazon ECR..."
aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com

echo "🔨 Building Docker image..."
docker buildx build --platform linux/amd64 --provenance=false -t ${IMAGE_URI} --load .

echo "📤 Pushing image to ECR..."
docker push ${IMAGE_URI}

echo "🚀 Updating Lambda function code..."
aws lambda update-function-code --function-name ${FUNCTION} --image-uri ${IMAGE_URI} --region ${REGION}

echo "✅ Deployment complete! Live at: https://2t2saofkd5ngmt7ahmrnaynujq0tutlw.lambda-url.ap-south-1.on.aws/exams"
```

To use it:
```bash
chmod +x deploy.sh
./deploy.sh
```

---

## 🗺️ Roadmap
- [ ] **Smart Subject Management:** Dashboard to manage and update note silos.
- [ ] **AI Grading Assistant:** Grade handwritten answers against stored keys.
- [ ] **User Auth:** Multi-teacher login support.
