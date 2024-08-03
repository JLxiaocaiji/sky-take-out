
#user  nobody; # 设置Nginx进程的用户和组
worker_processes  1; # Nginx工作进程的数量

#error_log  logs/error.log; # 指定错误日志的路径
#error_log  logs/error.log  notice; # 指定错误日志的级别，这里是notice
#error_log  logs/error.log  info;

#pid        logs/nginx.pid; # 指定进程ID文件的路径


events {    # 开始了一个事件块，用于配置Nginx事件模块
worker_connections  1024;   # 设置了每个工作进程的最大连接数，这里是1024
}


http { # HTTP块，用于配置Nginx HTTP模块
include       mime.types; # 包含了一个文件，该文件定义了MIME类型映射
default_type  application/octet-stream; # 设置了默认的MIME类型，当没有匹配的MIME类型时使用

    # 定义日志格式
    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '  # 指定访问日志的路径和格式
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main; # 指定访问日志的路径和格式

    sendfile        on; # 启用了sendfile传输模式，以提高性能
    #tcp_nopush     on; # TCP_NOPUSH选项，以减少网络拥塞

    #keepalive_timeout  0; # 超时即关闭keepalive连接
    keepalive_timeout  65;  

    #gzip  on;  # gzip压缩
	
    # 处理WebSocket升级
	map $http_upgrade $connection_upgrade{
		default upgrade;    # 设置了默认值，如果HTTP升级头没有指定，则默认升级
		'' close;   # 设置了空字符串的值，如果HTTP升级头为空，则关闭连接
	}

    # 开始了一个上游块，用于定义后端服务器
	upstream webservers{
	  server 127.0.0.1:8080 weight=90 ; # 定义了一个后端服务器，其权重为90
	  #server 127.0.0.1:8088 weight=10 ;
	}

    # 服务器块
    server {
        listen       80;        # 监听的端口为80
        server_name  localhost; # 服务器名称

        #charset koi8-r;        # 设置服务器输出字符集为KOI8-R

        #access_log  logs/host.access.log  main; # 指定访问日志的路径和格式

        location / {    # 定义匹配URL路径的配置
            root   html/sky;    # 根目录，即请求的URL将相对于这个目录进行解析
            index  index.html index.htm;    # 设置了默认的index文件，当请求的是目录时，Nginx会尝试返回这些文件
        }

        #error_page  404    /404.html; # 设置404错误页面

        # redirect server error pages to the static page /50x.html  # 重定向服务器错误页面到静态页面
        #
        error_page   500 502 503 504  /50x.html; # 500、502、503和504错误页面
        location = /50x.html {  # 定义了50x错误页面的路径
            root   html;
        }

        # 反向代理,处理管理端发送的请求
        location /api/ {
			proxy_pass   http://localhost:8080/admin/;  # 反向代理，将请求代理到localhost的8080端口上的/admin/路径
            #proxy_pass   http://webservers/admin/;
        }
		
		# 反向代理,处理用户端发送的请求
        location /user/ {
            proxy_pass   http://webservers/user/;   # 请求代理到webservers的/user/路径
        }
		
		# WebSocket
		location /ws/ {
            proxy_pass   http://webservers/ws/; # 将WebSocket请求代理到webservers的/ws/路径
			proxy_http_version 1.1; # 设置了代理的HTTP版本为1.1
			proxy_read_timeout 3600s;   # 设置了代理的读超时时间为3600秒
			proxy_set_header Upgrade $http_upgrade; # 设置了代理头部的Upgrade为$http_upgrade的值
			proxy_set_header Connection "$connection_upgrade"; # 设置了代理头部的Connection为$connection_upgrade的值
        }

        # 将PHP脚本传递给监听127.0.0.1:9000的FastCGI服务器
        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
        #
        #location ~ \.php$ {
        #    root           html;
        #    fastcgi_pass   127.0.0.1:9000;
        #    fastcgi_index  index.php;
        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
        #    include        fastcgi_params;
        #}

        # deny access to .htaccess files, if Apache's document root
        # concurs with nginx's one
        #
        #location ~ /\.ht {
        #    deny  all;
        #}
    }


    # another virtual host using mix of IP-, name-, and port-based configuration
    #
    #server {
    #    listen       8000;
    #    listen       somename:8080;
    #    server_name  somename  alias  another.alias;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}


    # HTTPS server
    
    # server {
    #    listen       443 ssl; # 服务器监听的端口为443
    #    server_name  localhost; # 服务器名称

    #    ssl_certificate      cert.pem;   # 服务器证书的路径，这里是cert.pem
    #    ssl_certificate_key  cert.key;   # 服务器证书私钥的路径，这里是cert.key

    #    ssl_session_cache    shared:SSL:1m;  # SSL会话缓存，以提高性能
    #    ssl_session_timeout  5m; # SSL会话超时时间为5分钟

    #    ssl_ciphers  HIGH:!aNULL:!MD5;   # 定义了SSL使用的密码套件，要求使用强密码套件，并禁用不安全的密码套件
    #    ssl_prefer_server_ciphers  on;   # 服务器优先的密码套件

    #     # 定义匹配根路径
    #    location / {
    #        root   html;
    #        index  index.html index.htm; # 默认的index文件，当请求的是目录时，Nginx会尝试返回这些文件
    #    }
    # }

}
