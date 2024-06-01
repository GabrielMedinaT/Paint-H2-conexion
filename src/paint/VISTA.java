package paint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.sql.*;

public class VISTA extends javax.swing.JFrame {

    private PAINT paint;
    private CONTROLADOR controlador; // Variable de instancia para CONTROLADOR

    private int nPoints = 3;
    private int clickCount = 0;
    private int[] vLinea = new int[3];
    private int[] xPoints = new int[nPoints];
    private int[] yPoints = new int[nPoints];
    private boolean MouseClickedOnce = false;
    private boolean fill = false;
    private List<Point> polygonPoints = new ArrayList<>();
    private JDialog colorDialog; // Diálogo para el selector de color

    public VISTA() {
        paint = new PAINT();
        controlador = new CONTROLADOR(paint, this); // Inicializa la variable de instancia controlador
        initComponents();
        initColorDialog(); // Inicializa el diálogo del selector de color
    
        // Establecer el color inicial a negro
        jColorChooser2.setColor(Color.BLACK);
        paint.setColor(Color.BLACK);
    
        try {
            controlador.poblarComboBox(cargar);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        cargar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cargarActionPerformed(evt);
            }
        });
    
        ((AbstractDocument) nombreArchivo.getDocument()).setDocumentFilter(new DocumentFilter() {
            private final int MAX_LENGTH = 20;
    
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) {
                    return;
                }
    
                if ((fb.getDocument().getLength() + string.length()) <= MAX_LENGTH) {
                    super.insertString(fb, offset, string, attr);
                }
            }
    
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) {
                    return;
                }
    
                if ((fb.getDocument().getLength() + text.length() - length) <= MAX_LENGTH) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
    
            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
            }
        });
    
        // Deshabilitar la función de pegar
        InputMap inputMap = nombreArchivo.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(KeyStroke.getKeyStroke("ctrl V"), "none");
        inputMap.put(KeyStroke.getKeyStroke("meta V"), "none"); 
    
        areaDibujo.setBackground(Color.WHITE);
        jSlider1.setValue(3);
        jLabel2.setText("3");
        jSlider1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int value = jSlider1.getValue();
                jLabel2.setText(Integer.toString(value));
                nPoints = value;
            }
        });
    
        jColorChooser2.getSelectionModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Color newColor = jColorChooser2.getColor();
                paint.setColor(newColor);
            }
        });
    
        seleccionarPunto.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    areaDibujo.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            Color color = jColorChooser2.getColor();
                            Graphics g = areaDibujo.getGraphics();
                            controlador.dibujarPunto(e.getX(), e.getY(), g, color);
                        }
                    });
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    areaDibujo.removeMouseListener(areaDibujo.getMouseListeners()[0]);
                }
            }
        });
    
        seleccionarRecta.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    areaDibujo.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            Color color = jColorChooser2.getColor();
                            if (MouseClickedOnce) {
                                Graphics g = areaDibujo.getGraphics();
                                controlador.dibujarRecta(e.getX(), e.getY(), vLinea[1], vLinea[2], g, color);
                                MouseClickedOnce = false;
                            } else {
                                Graphics g = areaDibujo.getGraphics();
                                vLinea[1] = e.getX();
                                vLinea[2] = e.getY();
                                MouseClickedOnce = true;
                            }
                        }
                    });
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    areaDibujo.removeMouseListener(areaDibujo.getMouseListeners()[0]);
                }
            }
        });
    
        seleccionarCircunferencia.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    areaDibujo.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            Color color = jColorChooser2.getColor();
                            if (MouseClickedOnce) {
                                Graphics g = areaDibujo.getGraphics();
                                controlador.dibujarCircunferencia(e.getX(), e.getY(), vLinea[1], vLinea[2], g, color);
                                MouseClickedOnce = false;
                            } else {
                                Graphics g = areaDibujo.getGraphics();
                                vLinea[1] = e.getX();
                                vLinea[2] = e.getY();
                                MouseClickedOnce = true;
                            }
                        }
                    });
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    areaDibujo.removeMouseListener(areaDibujo.getMouseListeners()[0]);
                }
            }
        });
    
        seleccionarPoligonoRegular.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    areaDibujo.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            Color color = jColorChooser2.getColor();
                            if (MouseClickedOnce) {
                                Graphics g = areaDibujo.getGraphics();
                                controlador.dibujarPoligonoR(e.getX(), e.getY(), vLinea[1], vLinea[2], nPoints, g, color);
                                MouseClickedOnce = false;
                                polygonPoints.add(new Point(e.getX(), e.getY())); // Añadir el punto final
                                savePolygonCoordinates(); // Guardar las coordenadas en un archivo
                                System.out.println("punto 1: " + e.getX() + " punto 2: " + e.getY() + " numero Lados " + nPoints);
                            } else {
                                Graphics g = areaDibujo.getGraphics();
                                vLinea[1] = e.getX();
                                vLinea[2] = e.getY();
                                MouseClickedOnce = true;
                                polygonPoints.clear(); // Limpiar la lista de puntos para un nuevo polígono
                                polygonPoints.add(new Point(vLinea[1], vLinea[2])); // Añadir el punto inicial
                            }
                        }
                    });
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    areaDibujo.removeMouseListener(areaDibujo.getMouseListeners()[0]);
                }
            }
        });
    
        seleccionarPoligonoIrregular.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    xPoints = new int[nPoints];
                    yPoints = new int[nPoints];
                    clickCount = 0;
    
                    areaDibujo.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (clickCount < nPoints) {
                                xPoints[clickCount] = e.getX();
                                yPoints[clickCount] = e.getY();
                                clickCount++;
                                System.out.println("PUNTO CREADO: (" + e.getX() + ", " + e.getY() + ")");
    
                                if (clickCount == nPoints) {
                                    Graphics g = areaDibujo.getGraphics();
                                    Color color = jColorChooser2.getColor();
                                    controlador.dibujarPoligonoI(xPoints, yPoints, nPoints, g, color);
                                    System.out.println("FIGURA CREADA");
    
                                    areaDibujo.removeMouseListener(this);
                                }
                            }
                        }
                    });
    
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    for (MouseListener listener : areaDibujo.getMouseListeners()) {
                        areaDibujo.removeMouseListener(listener);
                    }
                }
            }
        });
    }
    

    private void initColorDialog() {
        jColorChooser2.setColor(Color.BLACK); // Establecer el color predeterminado a negro
        colorDialog = new JDialog(this, "Seleccionar Color", Dialog.ModalityType.APPLICATION_MODAL);
        colorDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        colorDialog.getContentPane().add(jColorChooser2);
        colorDialog.pack();
    }
    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jRadioButton1 = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel5 = new javax.swing.JPanel();
        jColorChooser1 = new javax.swing.JColorChooser();
        jColorChooser2 = new javax.swing.JColorChooser();
        seleccionarRecta = new javax.swing.JRadioButton();
        areaDibujo = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        guardar = new javax.swing.JButton();
        cargar = new javax.swing.JComboBox<>();
        etiquetaNombre = new javax.swing.JLabel();
        nombreArchivo = new javax.swing.JTextField();
        CARGARBOTONDEM = new javax.swing.JButton();
        seleccionarPunto = new javax.swing.JRadioButton();
        seleccionarCircunferencia = new javax.swing.JRadioButton();
        seleccionarPoligonoRegular = new javax.swing.JRadioButton();
        seleccionarPoligonoIrregular = new javax.swing.JRadioButton();
        etiquetaNumeroVertices = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        jLabel2 = new javax.swing.JLabel();
        rellenarFigura = new javax.swing.JCheckBox();
        limpiar = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        jRadioButton1.setText("jRadioButton1");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 271, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jColorChooser2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(1280, 720));
        setResizable(false);

        buttonGroup1.add(seleccionarRecta);
        seleccionarRecta.setToolTipText("");
        seleccionarRecta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        seleccionarRecta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/paint/Images/Linea.png"))); // NOI18N
        seleccionarRecta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seleccionarRectaActionPerformed(evt);
            }
        });

        areaDibujo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout areaDibujoLayout = new javax.swing.GroupLayout(areaDibujo);
        areaDibujo.setLayout(areaDibujoLayout);
        areaDibujoLayout.setHorizontalGroup(
            areaDibujoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        areaDibujoLayout.setVerticalGroup(
            areaDibujoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 671, Short.MAX_VALUE)
        );

        jButton1.setText("ELIMINAR");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        guardar.setText("GUARDAR");
        guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guardarActionPerformed(evt);
            }
        });

        cargar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cargarActionPerformed(evt);
            }
        });

        etiquetaNombre.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        etiquetaNombre.setText("NOMBRE");

        nombreArchivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nombreArchivoActionPerformed(evt);
            }
        });

        CARGARBOTONDEM.setText("Cargar");
        CARGARBOTONDEM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CARGARBOTONDEMActionPerformed(evt);
            }
        });

        buttonGroup1.add(seleccionarPunto);
        seleccionarPunto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        seleccionarPunto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/paint/Images/Punto.png"))); // NOI18N
        seleccionarPunto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seleccionarPuntoActionPerformed(evt);
            }
        });

        buttonGroup1.add(seleccionarCircunferencia);
        seleccionarCircunferencia.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        seleccionarCircunferencia.setIcon(new javax.swing.ImageIcon(getClass().getResource("/paint/Images/Circunferencia.png"))); // NOI18N
        seleccionarCircunferencia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seleccionarCircunferenciaActionPerformed(evt);
            }
        });

        buttonGroup1.add(seleccionarPoligonoRegular);
        seleccionarPoligonoRegular.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        seleccionarPoligonoRegular.setIcon(new javax.swing.ImageIcon(getClass().getResource("/paint/Images/poligono.png"))); // NOI18N
        seleccionarPoligonoRegular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seleccionarPoligonoRegularActionPerformed(evt);
            }
        });

        buttonGroup1.add(seleccionarPoligonoIrregular);
        seleccionarPoligonoIrregular.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        seleccionarPoligonoIrregular.setIcon(new javax.swing.ImageIcon(getClass().getResource("/paint/Images/Irregular.png"))); // NOI18N
        seleccionarPoligonoIrregular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seleccionarPoligonoIrregularActionPerformed(evt);
            }
        });

        etiquetaNumeroVertices.setText("LADOS");

        jSlider1.setMaximum(20);
        jSlider1.setMinimum(3);
        jSlider1.setPaintLabels(true);
        jSlider1.setSnapToTicks(true);

        jLabel2.setText("20");
        jLabel2.setToolTipText("");
        jLabel2.setVerifyInputWhenFocusTarget(false);

        rellenarFigura.setText("Rellenar");
        rellenarFigura.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rellenarFigura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rellenarFiguraActionPerformed(evt);
            }
        });

        limpiar.setText("BORRAR DIBUJO");
        limpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limpiarActionPerformed(evt);
            }
        });

        jButton2.setText("Color");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cargar, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CARGARBOTONDEM)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seleccionarPunto)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seleccionarRecta)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seleccionarCircunferencia)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seleccionarPoligonoRegular)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seleccionarPoligonoIrregular, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(etiquetaNumeroVertices)
                        .addGap(18, 18, 18)
                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rellenarFigura)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)
                        .addGap(46, 46, 46)
                        .addComponent(limpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(etiquetaNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nombreArchivo, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guardar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addComponent(areaDibujo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(seleccionarCircunferencia, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(seleccionarPoligonoRegular, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(seleccionarRecta, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(seleccionarPoligonoIrregular)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(CARGARBOTONDEM)
                        .addComponent(cargar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(etiquetaNombre)
                        .addComponent(limpiar)
                        .addComponent(nombreArchivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(guardar)
                        .addComponent(jButton1)
                        .addComponent(jButton2)
                        .addComponent(rellenarFigura))
                    .addComponent(seleccionarPunto, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(etiquetaNumeroVertices)
                        .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(areaDibujo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void rellenarFiguraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rellenarFiguraActionPerformed
        if (rellenarFigura.isSelected()) {
            fill = true;
            paint.setRelleno(true);
           
        } else {
            fill = false;
            paint.setRelleno(false);
            
        }
    }//GEN-LAST:event_rellenarFiguraActionPerformed

    private void seleccionarPuntoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seleccionarPuntoActionPerformed
        if (seleccionarPunto.isSelected()) {
            seleccionarPunto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/paint/Images/puntoSelecionado.png")));
        } else {
            seleccionarPunto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/paint/Images/Punto.png")));
        }
    }//GEN-LAST:event_seleccionarPuntoActionPerformed

    private void guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarActionPerformed
        String nombre = nombreArchivo.getText();
        boolean valido = true;

        if ("".equals(nombre)) {
            valido = false;
            JOptionPane.showMessageDialog(null, "EL DIBUJO DEBE TENER UN NOMBRE PARA SER GUARDADO", "ERROR", JOptionPane.ERROR_MESSAGE);
        } else {
            if (nombre.matches("[a-zA-ZñÑ1234567890_]+")) {
                System.out.println("NOMBRE VALIDO");
            } else {
                valido = false;
                JOptionPane.showMessageDialog(null, "NOMBRE DE DIBUJO CON CARACTERES INVÁLIDOS, SOLO NÚMEROS, LETRAS Y _ PERMITIDOS", "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }

        if (valido) {
            try {
                controlador.guardarFicheroYPoligonos(nombre);
                JOptionPane.showMessageDialog(null, "Dibujo guardado correctamente.", "ÉXITO", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error al guardar el dibujo: " + e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_guardarActionPerformed

    private void seleccionarPoligonoRegularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seleccionarPoligonoRegularActionPerformed

    }//GEN-LAST:event_seleccionarPoligonoRegularActionPerformed

    private void nombreArchivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nombreArchivoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nombreArchivoActionPerformed

    private void cargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cargarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cargarActionPerformed

    private void seleccionarCircunferenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seleccionarCircunferenciaActionPerformed

    }//GEN-LAST:event_seleccionarCircunferenciaActionPerformed

    private void seleccionarPoligonoIrregularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seleccionarPoligonoIrregularActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_seleccionarPoligonoIrregularActionPerformed

    private void cargarBotonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cargarBotonActionPerformed
        // TODO add your handling code here:
        String selectedFichero = (String) cargar.getSelectedItem();
        try {
            controlador.cargarDibujo(selectedFichero, areaDibujo.getGraphics());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_cargarBotonActionPerformed

    private void CARGARBOTONDEMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CARGARBOTONDEMActionPerformed
        // TODO add your handling code here:

        String selectedFichero = (String) cargar.getSelectedItem();
        try {
            Graphics g = areaDibujo.getGraphics();
            //limpiarPanel(g); // Limpiar el panel antes de cargar el nuevo dibujo
            controlador.cargarDibujo(selectedFichero, g);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_CARGARBOTONDEMActionPerformed

    private void limpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limpiarActionPerformed
        // TODO add your handling code here:

        limpiarPanel(areaDibujo);
    }//GEN-LAST:event_limpiarActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String selectedFichero = (String) cargar.getSelectedItem();

        // Mostrar ventana de advertencia para confirmar la eliminación
        int confirm = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar el fichero " + selectedFichero + "? Esta acción no se puede deshacer ", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Llamar al controlador para eliminar el fichero
                controlador.eliminarFichero(selectedFichero);

                // Actualizar el JComboBox después de la eliminación
                controlador.poblarComboBox(cargar);

                // Mostrar mensaje de éxito
                JOptionPane.showMessageDialog(this, "Fichero eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                // Manejar errores de la base de datos
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al eliminar el fichero: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void seleccionarRectaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seleccionarRectaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_seleccionarRectaActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
         colorDialog.setLocationRelativeTo(this);
            colorDialog.setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void savePolygonCoordinates() {
        try (FileWriter writer = new FileWriter("polygon_coordinates.txt", true)) {
            writer.write("Polígono Regular: \n");
            for (Point point : polygonPoints) {
                writer.write("Punto: (" + point.x + ", " + point.y + ")\n");
            }
            writer.write("\n");
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void limpiarPanel(JPanel panel) {
        Graphics g = panel.getGraphics();
        g.setColor(panel.getBackground());
        g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
        g.dispose(); // Libera los recursos del Graphics
    }

    public int getAreaDibujoWidth() {
        return areaDibujo.getWidth();
    }

    public int getAreaDibujoHeight() {
        return areaDibujo.getHeight();
    }

    public Color getSelectedColor() {
        return jColorChooser2.getColor();
    }

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CARGARBOTONDEM;
    private javax.swing.JPanel areaDibujo;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cargar;
    private javax.swing.JLabel etiquetaNombre;
    private javax.swing.JLabel etiquetaNumeroVertices;
    private javax.swing.JButton guardar;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JColorChooser jColorChooser1;
    private javax.swing.JColorChooser jColorChooser2;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JButton limpiar;
    private javax.swing.JTextField nombreArchivo;
    private javax.swing.JCheckBox rellenarFigura;
    private javax.swing.JRadioButton seleccionarCircunferencia;
    private javax.swing.JRadioButton seleccionarPoligonoIrregular;
    private javax.swing.JRadioButton seleccionarPoligonoRegular;
    private javax.swing.JRadioButton seleccionarPunto;
    private javax.swing.JRadioButton seleccionarRecta;
    // End of variables declaration//GEN-END:variables
}
//VSCODE
