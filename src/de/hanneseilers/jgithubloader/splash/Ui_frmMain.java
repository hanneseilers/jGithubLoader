/********************************************************************************
 ** Form generated from reading ui file 'splash.jui'
 **
 ** Created by: Qt User Interface Compiler version 4.8.6
 **
 ** WARNING! All changes made in this file will be lost when recompiling ui file!
 ********************************************************************************/
package de.hanneseilers.jgithubloader.splash;
import com.trolltech.qt.core.*;

import com.trolltech.qt.gui.*;

public class Ui_frmMain implements com.trolltech.qt.QUiForm<QMainWindow>
{
    public QWidget centralwidget;
    public QFrame hline;
    public QLabel lblProgress;
    public QLabel lblAppName;

    public Ui_frmMain() { super(); }

    public void setupUi(QMainWindow frmMain)
    {
        frmMain.setObjectName("frmMain");
        frmMain.resize(new QSize(384, 103).expandedTo(frmMain.minimumSizeHint()));
        frmMain.setWindowOpacity(0.6);
        centralwidget = new QWidget(frmMain);
        centralwidget.setObjectName("centralwidget");
        hline = new QFrame(centralwidget);
        hline.setObjectName("hline");
        hline.setGeometry(new QRect(0, 60, 381, 20));
        hline.setFrameShape(QFrame.Shape.HLine);
        lblProgress = new QLabel(centralwidget);
        lblProgress.setObjectName("lblProgress");
        lblProgress.setGeometry(new QRect(0, 80, 381, 20));
        lblAppName = new QLabel(centralwidget);
        lblAppName.setObjectName("lblAppName");
        lblAppName.setGeometry(new QRect(0, 0, 381, 61));
        QFont font = new QFont();
        font.setPointSize(20);
        font.setBold(true);
        font.setWeight(75);
        lblAppName.setFont(font);
        lblAppName.setAlignment(com.trolltech.qt.core.Qt.AlignmentFlag.createQFlags(com.trolltech.qt.core.Qt.AlignmentFlag.AlignCenter));
        frmMain.setCentralWidget(centralwidget);
        retranslateUi(frmMain);
        frmMain.connectSlotsByName();
    } // setupUi

    void retranslateUi(QMainWindow frmMain)
    {
        frmMain.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("frmMain", "MainWindow", null));
        lblProgress.setText(com.trolltech.qt.core.QCoreApplication.translate("frmMain", "initiating...", null));
        lblAppName.setText(com.trolltech.qt.core.QCoreApplication.translate("frmMain", "jGithubLoader", null));
    } // retranslateUi

}

