package org.example.user;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.function.ToDoubleBiFunction;

public final class NetUser {

    static final String HOST = "127.0.0.1";
    static final int PORT = 8002;

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new StringDecoder());
                            p.addLast(new StringEncoder());
                            p.addLast(new NetUserHandler());
                        }
                    });

            ChannelFuture f = b.connect(HOST, PORT).sync();
            Channel channel = f.sync().channel();
            //TODO додати Exception
            Scanner scanner = new Scanner(System.in);
            while (true) {
                CountDownLatch latch = new CountDownLatch(1);
                NetUserHandler handler = (NetUserHandler) channel.pipeline().last();
                handler.setLatch(latch);

                System.out.print("Enter username or 'exit' to quit: ");
                String username = scanner.nextLine();
                if ("exit".equalsIgnoreCase(username)) {
                    break;
                }
                System.out.print("Enter email: ");
                String email = scanner.nextLine();
                String userInfo = "Username: " + username + ", Email: " + email;
                channel.writeAndFlush(userInfo).sync();

                latch.await();
            }

            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}