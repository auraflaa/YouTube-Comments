FROM nginx:alpine
# Copy static frontend files
COPY . /usr/share/nginx/html
# Copy Nginx config from the infra folder during build if desired, 
# but usually it's better to keep the image generic and mount the config,
# or COPY it here if we want a standalone image.
# Let's COPY it so the image is standalone.
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
