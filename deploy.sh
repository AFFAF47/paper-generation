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