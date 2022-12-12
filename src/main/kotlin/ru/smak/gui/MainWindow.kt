package ru.smak.gui

import ru.smak.graphics.*
import ru.smak.math.Complex
import ru.smak.math.Julia
import ru.smak.math.Mandelbrot
import ru.smak.video.ui.windows.VideoWindow
import java.awt.Button
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.*
import kotlin.random.Random


open class MainWindow : JFrame() {
    private var rect: Rectangle = Rectangle()
    val minSz = Dimension(800, 600)
    val mainPanel: GraphicsPanel

    private val _videoWindow = VideoWindow(this).apply { isVisible = false; };



    val trgsz = TargetSz()

    init {
        val menuBar = JMenuBar().apply {
            add(createColorMenu())
            add(createCtrlZButton())
            add(createAboutButton())
        }

        jMenuBar = menuBar

        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = minSz

        colorScheme = ColorFuncs[Random.nextInt(ColorFuncs.size)]

        val plane = Plane(-2.0, 1.0, -1.0, 1.0)
        trgsz.getTargetFromPlane(plane)
        val fp = FractalPainter(Mandelbrot()::isInSet, colorScheme, plane)
        //val fpj = FractalPainter(Julia()::isInSet, ::testFunc, plane)
        mainPanel = GraphicsPanel().apply {
            background = Color.WHITE
            addPainter(fp)
            //addPainter(fpj)

        }

        mainPanel.addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                super.componentResized(e)
                plane.width=mainPanel.width
                plane.height=mainPanel.height
                makeOneToOne(plane, trgsz, mainPanel.size)//Делает панель мастштабом 1 к 1
            }
        })


        menuBar.add(createRecordBtn(plane)); // создаем окошко для создания видео


        mainPanel.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    super.mouseClicked(e)
                    e?.let {
                        if (e.button == MouseEvent.BUTTON1) {
                            SecondWindow(colorScheme).apply {
                                Julia.selectedPoint =
                                    Complex(Converter.xScrToCrt(e.x, plane), Converter.yScrToCrt(e.y, plane))
                                isVisible = true
                            }
                        }
                    }
                }
            })

        mainPanel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                e?.let {
                    rect.addPoint(it.point)
                }
            }

            override fun mouseReleased(e: MouseEvent?) {
                super.mouseReleased(e)
                rect.leftTop?.let { first ->
                    val g = mainPanel.graphics
                    g.color = Color.BLACK
                    g.setXORMode(Color.WHITE)
                    g.drawRect(first.x, first.y, rect.width, rect.height)
                    g.setPaintMode()
                    if (rect.isExistst) {
                        val x1 = rect.x1?.let { Converter.xScrToCrt(it, plane) } ?: return@let
                        val x2 = rect.x2?.let { Converter.xScrToCrt(it, plane) } ?: return@let
                        val y1 = rect.y1?.let { Converter.yScrToCrt(it, plane) } ?: return@let
                        val y2 = rect.y2?.let { Converter.yScrToCrt(it, plane) } ?: return@let
                        makeOneToOne(
                            plane,
                            x1,
                            x2,
                            y1,
                            y2,
                            mainPanel.size,
                            trgsz
                        )//Делает панель мастштабом 1 к 1 и меняет trgsz
                        mainPanel.repaint()
                    }
                }
                rect.destroy()
            }
        })

        mainPanel.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                super.mouseDragged(e)

                e?.let { curr ->
                    rect.leftTop?.let { first ->
                        val g = mainPanel.graphics
                        g.color = Color.BLACK
                        g.setXORMode(Color.WHITE)
                        if (rect.isExistst)
                            g.drawRect(first.x, first.y, rect.width, rect.height)
                        rect.addPoint(curr.point)
                        rect.leftTop?.let { f -> g.drawRect(f.x, f.y, rect.width, rect.height) }
                        g.setPaintMode()
                    }
                }
            }
        })


        layout = GroupLayout(contentPane).apply {
            setHorizontalGroup(
                createSequentialGroup()
                    .addGap(8)
                    .addComponent(mainPanel, GROW, GROW, GROW)
                    .addGap(8)
            )

            setVerticalGroup(
                createSequentialGroup()
                    .addGap(8)
                    .addComponent(mainPanel, GROW, GROW, GROW)
                    .addGap(8)
            )
        }
    }

    class AboutWindow : JFrame() {
        val minSz = Dimension(400, 450)

        val commonLabel: JLabel
        val pplLabel1: JLabel
        val pplLabel2: JLabel
        val pplLabel3 = JLabel("Цымбал Данила");


        init {
            commonLabel = JLabel()
            commonLabel.text = "Над проектом работали : "
            pplLabel1 = JLabel()
            pplLabel1.text = "Потасьев Никита"
            pplLabel2 = JLabel()
            pplLabel2.text = "Щербанев Дмитрий"

            minimumSize = minSz

            layout = GroupLayout(contentPane).apply {
                setHorizontalGroup(
                    createSequentialGroup()
                        .addGap(8)
                        .addGroup(
                            createParallelGroup()
                                .addComponent(commonLabel, SHRINK, SHRINK, SHRINK)
                        )
                        .addGap(16)
                        .addGroup(
                            createParallelGroup()
                                .addComponent(pplLabel1, SHRINK, SHRINK, SHRINK)
                                .addComponent(pplLabel2, SHRINK, SHRINK, SHRINK)
                                .addComponent(pplLabel3, SHRINK, SHRINK, SHRINK)
                        )
                        .addGap(8)
                )

                setVerticalGroup(
                    createSequentialGroup()
                        .addGap(8)
                        .addGroup(
                            createParallelGroup()
                                .addComponent(commonLabel, SHRINK, SHRINK, SHRINK)
                                .addComponent(pplLabel1, SHRINK, SHRINK, SHRINK)
                        )
                        .addGroup(
                            createParallelGroup()
                                .addComponent(pplLabel2, SHRINK, SHRINK, SHRINK)
                        )
                        .addGroup(
                            createParallelGroup()
                                .addComponent(pplLabel3, SHRINK, SHRINK, SHRINK)
                        )
                        .addGap(8)
                )
            }
        }
    }

    private fun createAboutButton(): JButton {
        val aboutButton = JButton("О программе")
        aboutButton.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                e?.let {
                    val frame = AboutWindow()
                    frame.isVisible = true
                    frame.defaultCloseOperation = DISPOSE_ON_CLOSE

                }
            }
        })
        return aboutButton

    }


    private fun createColorMenu(): JMenu {
        val colorMenu = JMenu("Выбор цветовой гаммы")

        val mClr = JColorChooser()
        val sClr = JColorChooser()

        var firstColor: Color
        var secondColor: Color

        firstColor = mClr.selectionModel.selectedColor
        secondColor = sClr.selectionModel.selectedColor

        colorMenu.add(mClr)
        colorMenu.add(sClr)


        return colorMenu

    }

    private fun createCtrlZButton(): JButton {
        val ctrlzButton = JButton("Отменить предыдущее действие")

        return ctrlzButton

    }

    private fun createRecordBtn(plane: Plane): JButton {
        val btn = JButton("Record");

        btn.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                super.mousePressed(e)
                e?.let {
                    _videoWindow.apply {
                        this.plane = plane;
                        isVisible = true
                    }
                }
            }
        })
        return btn;
    }

    override fun setVisible(b: Boolean) {
        super.setVisible(b)
        mainPanel.graphics.run {
            setXORMode(Color.WHITE)
            drawLine(-100, -100, -101, -100)
            setPaintMode()
        }
    }

    companion object {
        const val GROW = GroupLayout.DEFAULT_SIZE
        const val SHRINK = GroupLayout.PREFERRED_SIZE

        var colorScheme: (Float) -> Color = ::testFunc;
    }

    // TODO: for testing video creation
    fun getScreenShot(width: Int, height: Int): BufferedImage {

        val image = BufferedImage(
            width,
            height,
            BufferedImage.TYPE_INT_RGB
        )
        mainPanel.paint(image.graphics)
        return image
    }


}