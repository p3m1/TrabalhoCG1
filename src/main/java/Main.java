import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;


public class Main {

    // The window handle
    private long window;
    private GLFWCursorPosCallback mouseCallback;
    String state;

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
        });

//        glfwSetCursorPosCallback(window, mouseCallback = new MouseHandler());


        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    DoubleBuffer posX = BufferUtils.createDoubleBuffer(1);
    DoubleBuffer posY = BufferUtils.createDoubleBuffer(1);
    double x0, y0, x1, y1;

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

        glOrtho(0,800,600,0,0,1);
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            glfwGetCursorPos(window, posX, posY);
            double x = posX.get(0);
            double y = posY.get(0);
            boolean teste = false;
            if(state=="pressed"){
                x0 = x;
                y0 = y;
                teste = true;
                while(teste){
                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                    glfwGetCursorPos(window, posX, posY);
                    desenhaLinha(x0, y0, posX.get(0), posY.get(0));
                    glfwSwapBuffers(window); // swap the color buffers
                    // Poll for window events. The key callback above will only be
                    // invoked during this call.
                    glfwPollEvents();
                    if(state=="released")
                        teste = false;
                }
                if(state=="released"){
                    x1 = posX.get(0);
                    y1 = posY.get(0);
                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                    glfwGetCursorPos(window, posX, posY);
                    desenhaLinha(x0, y0, posX.get(0), posY.get(0));
                    glfwSwapBuffers(window); // swap the color buffers
                    // Poll for window events. The key callback above will only be
                    // invoked during this call.
                    glfwPollEvents();
                }
            }
            desenhaLinha(x0,y0,x1,y1);
            System.out.println(x+ " "+y);
            glfwSwapBuffers(window); // swap the color buffers
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }


    public void desenhaLinha(double x0, double y0, double x1, double y1){
        double dx = 0, dy = 0, incE, incNE, d, x, y;

        boolean dx_menor_dy = false;

        int dy_signal = 1;

        dx = x1 - x0;
        dy = y1 - y0;

        if(Math.abs(dx) < Math.abs(dy)){
            dx_menor_dy = true;

            double aux = dx;
            dx = dy;
            dy = aux;


            aux = x0;
            x0 = y0;
            y0 = aux;

            aux = x1;
            x1 = y1;
            y1 = aux;
        }

        if(x1<x0){
            double aux = x1;
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
            if (!dx_menor_dy) {
                desenhaPixel(x, y);
            } else {
                desenhaPixel(y, x);
            }

            x ++;
            if(d <= 0){
                d = d + incE;
            } else {
                d = d + incNE;
                y += dy_signal;
            }
        }
    }

    public void desenhaPixel(double x, double y){
        glColor3f(0.0f,0.0f,0.0f);
        glBegin(GL_POINTS);
            glColor3i(1,1,1);
            glVertex2d(x,y);
        glEnd();

//        glBegin(GL_POINTS);
//            glColor3i(1,0,0);
//            for(double d=0; d<1; d+=0.001 )
//            glVertex2d(d, d);
//            glEnd();
    }

    public static void main(String[] args) {
        new Main().run();
    }

}

