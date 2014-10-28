/var/log/obm-services/obm-services.log {
    weekly
    rotate 4
    compress
    delaycompress
    create 640 apache apache
    missingok
}
