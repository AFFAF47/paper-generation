# 🎓 AI-Powered Exam Paper Generator

A sophisticated full-stack Spring Boot application that utilizes **RAG (Retrieval-Augmented Generation)** to transform personal PDF notes into structured school exam papers. This project features a distributed architecture, offloading heavy AI computation to a dedicated local server.

---

## 🧠 System Architecture & Workflow
The project is built on the principle of **Retrieval-Augmented Generation (RAG)**. Instead of relying on a model's general knowledge, it "retrieves" your specific notes to "augment" the AI's generation.

### 1. Ingestion (The Library)
* [cite_start]**PDF Processing:** Documents are parsed and broken into smaller text "chunks"[cite: 6].
* **Vector Embeddings:** Text is converted into mathematical vectors using the `nomic-embed-text` model on a remote Windows PC.
* [cite_start]**Vector Storage:** These vectors are stored in **Pinecone**, organized by Subject and Class metadata for precise retrieval[cite: 5, 6].

### 2. Retrieval & Generation (The Brain)
* [cite_start]**Similarity Search:** When an exam is requested, the system finds the most relevant chunks in Pinecone based on the chapter name[cite: 1, 5].
* **Contextual Prompting:** The retrieved text is injected into a "Teacher Prompt" sent to **Llama 3.1**.
* [cite_start]**Execution:** The LLM generates questions (MCQs, Long Answers) and an Answer Key based *only* on the provided notes[cite: 9, 11, 12, 15].

### 3. Storage & Export (The Archive)
* [cite_start]**MongoDB Atlas:** Every generated paper is saved to a cloud-hosted history log[cite: 2, 4].
* **Professional PDF:** Uses **OpenPDF** to generate branded A4 documents with automated page breaks for the answer key.

---

## 🛠️ Tech Stack
| Component | Technology |
| :--- | :--- |
| **Framework** | Spring Boot 3.x, Spring AI |
| **AI Models** | Llama 3.1 (LLM), nomic-embed-text (Embeddings) |
| **Vector DB** | Pinecone |
| **Database** | MongoDB Atlas |
| **Networking** | Tailscale (Secure Mesh VPN) |
| **Frontend** | Thymeleaf, Bootstrap 5 |
| **PDF Engine** | OpenPDF / LibrePDF |

---

## 🚀 Key Features
* **Distributed AI:** Seamlessly connects a MacBook client to a Windows AI server over any network via **Tailscale**.
* **Smart Subject Silos:** Metadata filtering ensures Physics questions only come from Physics notes.
* **Automated Answer Keys:** AI places answers at the end of the document after a strict delimiter.
* **Branded PDF Export:** Server-side PDF generation ensures consistent formatting with school logos.

---

## 📂 API Summary
* `GET /exams` - Access the main generator dashboard.
* `POST /api/exams/generate` - Trigger the AI pipeline and save to MongoDB.
* `GET /exams/history/results` - Search the Atlas database for past papers.
* `GET /api/pdf/download/{id}` - Generate and download the official PDF.

---

## 🗺️ Roadmap
- [ ] **Smart Subject Management:** Dashboard to manage and update note silos.
- [ ] **AI Grading Assistant:** Grade handwritten answers against stored keys.
- [ ] **User Auth:** Multi-teacher login support.

## Live
* Service is Live at `https://paper-generation-p9il.onrender.com/exams`
