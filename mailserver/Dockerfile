# FROM debian:bullseye
#
# ENV DEBIAN_FRONTEND=noninteractive
#
# # Cài đầy đủ Postfix, Dovecot và hỗ trợ MySQL
# RUN apt-get update && apt-get install -y \
#     postfix \
#     postfix-mysql \
#     dovecot-core \
#     dovecot-imapd \
#     dovecot-lmtpd \
#     dovecot-mysql \
#     libsasl2-modules \
#     mariadb-client \
#     dnsutils \
#     && rm -rf /var/lib/apt/lists/*
#
# RUN apt-get update && \
#     apt-get install -y mailutils
#
# # Tạo user system cho dovecot và postfix
# RUN adduser --system --group --no-create-home dovecot \
#     && adduser --system --group --no-create-home postfix
#
# # Tạo thư mục mailbox và cấp quyền cho UID/GID 5000
# RUN mkdir -p /var/mail/vhosts && \
#     chown -R 5000:5000 /var/mail/vhosts
#
# # Sao chép file cấu hình postfix
# COPY ./conf/postfix /etc/postfix
#
# # Đặt quyền an toàn cho toàn bộ thư mục cấu hình postfix
# RUN chown -R root:root /etc/postfix && \
#     find /etc/postfix -type f -exec chmod 644 {} \;
#
# # Sửa quyền file dynamicmaps.cf (nếu nó tồn tại sau khi COPY)
# RUN [ -f /etc/postfix/dynamicmaps.cf ] && \
#     chmod 644 /etc/postfix/dynamicmaps.cf && \
#     chown root:root /etc/postfix/dynamicmaps.cf || true
#
# # Sao chép entrypoint
# COPY ./entrypoint.sh /entrypoint.sh
# RUN chmod +x /entrypoint.sh
#
# EXPOSE 25 143 587
#
# #CMD ["/entrypoint.sh"]
# ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]


FROM debian:bullseye

ENV DEBIAN_FRONTEND=noninteractive

# Cài các gói
RUN apt-get update && apt-get install -y \
    postfix \
    postfix-mysql \
    dovecot-core \
    dovecot-imapd \
    dovecot-lmtpd \
    dovecot-mysql \
    libsasl2-modules \
    mariadb-client \
    dnsutils \
    mailutils \
 && rm -rf /var/lib/apt/lists/*

# Tạo user và thư mục
RUN adduser --system --group --no-create-home dovecot \
 && adduser --system --group --no-create-home postfix \
 && mkdir -p /var/mail/vhosts \
 && chown -R 5000:5000 /var/mail/vhosts

# Sao chép cấu hình postfix
COPY ./conf/postfix /etc/postfix

COPY ./conf/dovecot /etc/dovecot

RUN chown -R root:root /etc/postfix \
 && find /etc/postfix -type f -exec chmod 644 {} \; \
 && [ -f /etc/postfix/dynamicmaps.cf ] && chmod 644 /etc/postfix/dynamicmaps.cf && chown root:root /etc/postfix/dynamicmaps.cf || true

# Sao chép entrypoint
COPY ./entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 25 143 587

ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]
CMD ["/usr/sbin/dovecot", "-F"]
