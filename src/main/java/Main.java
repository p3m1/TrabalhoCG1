import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import org.lwjgl.Version;

import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;


public class Main {

    // The window handle
    private long window;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        try {
            init();
            loop();

            // Release window and window callbacks
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        } finally {
            // Terminate GLFW and release the GLFWerrorfun
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

        int WIDTH = 800;
        int HEIGHT = 600;

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Bresenham", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
        });

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (vidmode.width() - WIDTH) / 2,
                (vidmode.height() - HEIGHT) / 2
        );

        glfwSetMouseButtonCallback(window, (windowHnd, button, action, mods) -> {
            String state;
            switch (action) {
                case GLFW_RELEASE:
                    state = "released";
                    break;
                case GLFW_PRESS:
                    state = "pressed";
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unsupported mouse button action: 0x%X", action));
            }
            DoubleBuffer a1 = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer a2 = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer b1 = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer b2 = BufferUtils.createDoubleBuffer(1);
            if(state == "pressed") {
                glfwGetCursorPos(window, a1, a2);
                System.out.println("x : " + b1.get(0) + ", y = " + b2.get(0));
            }
            if(state == "released"){
                glfwGetCursorPos(window, b1, b2);
                System.out.println("x : " + b1.get(0) + ", y = " + b2.get(0));
                int x0 = (int) a1.get(0);
                int y0 = (int) a2.get(0);
                int x1 = (int) b1.get(0);
                int y1 = (int) b2.get(0);

                desenhaLinha(x0,y0,x1,y1);
            }
        });

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            glfwSwapBuffers(window); // swap the color buffers

            glColor3f(1.0f,0.0f,0.0f);
//            glBegin(GL_LINE_STRIP);
//            glVertex2i(0,0);
//            glVertex2i(1,1);
//            glEnd();
//            desenhaPixel(0,0);
            glfwSwapBuffers(window);

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    public void desenhaLinha(int x0, int y0, int x1, int y1){
        IntBuffer buffer = BufferUtils.createIntBuffer(4);
        glGetIntegerv(GL_VIEWPORT, buffer);
        int height = buffer.get(3);

        y0 = height - y0;
        y1 = height - y1;

        int dx = 0, dy = 0, incE, incNE, d, x, y;

        boolean dx_menor_dy = false;

        int dy_signal = 1;

        dx = x1 - x0;
        dy = y1 - y0;

        if(Math.abs(dx) < Math.abs(dy)){
            dx_menor_dy = true;

            int aux = dx;
            dx = dy;
            dy = aux;

            aux = x0;
            x0 = y0;
            y0 = aux;

            aux = x0;
            x0 = y0;
            y0 = aux;

            aux = x1;
            x1 = y1;
            y1 = aux;
        }

        if(x1<x0){
            int aux = x1;
            x1 = x0;
            x0 = aux;

            aux = y1;
            y1 = y0;
            y0 = aux;

            dx = -dx;
            dy = -dy;
        }

        if(dy<0){
            dy_signal = -1;
            dy = -dy;
        }

        d = 2 * dy - dx;
        incE = 2 * dy;
        incNE = 2 * (dy - dx);

        x = x0;
        y = y0;

        while(x <= x1){
            desenhaPixel(x,y);

            x++;
            if(d <= 0){
                d = d + incE;
            } else {
                d = d + incE;
                y += dy_signal;
            }
        }
    }

    public void desenhaPixel(int x, int y){
        glColor3f(0.0f,0.0f,0.0f);
        glBegin(GL_POINTS);
        glVertex2i(x,y);
        glEnd();
        glfwSwapBuffers(window);
    }

    public static void main(String[] args) {
        new Main().run();
    }

}

