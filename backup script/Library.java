/*
 * Copyright (C) 2002 - 2007 Lehrstuhl Grafische Systeme, BTU Cottbus
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package de.grogra.rgg;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3d;

import de.grogra.graph.BooleanAttribute;
import de.grogra.graph.ByteAttribute;
import de.grogra.graph.CharAttribute;
import de.grogra.graph.DoubleAttribute;
import de.grogra.graph.EdgePatternImpl;
import de.grogra.graph.FloatAttribute;
import de.grogra.graph.Graph;
import de.grogra.graph.GraphState;
import de.grogra.graph.IntAttribute;
import de.grogra.graph.LongAttribute;
import de.grogra.graph.ObjectAttribute;
import de.grogra.graph.Path;
import de.grogra.graph.ShortAttribute;
import de.grogra.graph.VisitorImpl;
import de.grogra.graph.impl.Edge;
import de.grogra.graph.impl.GraphManager;
import de.grogra.graph.impl.Node;
import de.grogra.imp.IMP;
import de.grogra.imp.IMPWorkbench;
import de.grogra.imp.View;
import de.grogra.imp.objects.ImageRef;
import de.grogra.imp3d.Camera;
import de.grogra.imp3d.DisplayVisitor;
import de.grogra.imp3d.View3D;
import de.grogra.imp3d.ViewConfig3D;
import de.grogra.imp3d.VolumeAttribute;
import de.grogra.imp3d.objects.Box;
import de.grogra.imp3d.objects.GRSVertex;
import de.grogra.imp3d.objects.GlobalTransformation;
import de.grogra.imp3d.objects.Instance3D;
import de.grogra.imp3d.objects.LightDistributionRef;
import de.grogra.imp3d.objects.Matrix34dPair;
import de.grogra.imp3d.objects.MeshNode;
import de.grogra.imp3d.objects.NURBSCurve;
import de.grogra.imp3d.objects.NURBSSurface;
import de.grogra.imp3d.objects.Null;
import de.grogra.imp3d.objects.Parallelogram;
import de.grogra.imp3d.objects.PolygonMesh;
import de.grogra.imp3d.objects.ShadedNull;
import de.grogra.imp3d.objects.SpectrumRef;
import de.grogra.imp3d.objects.Sphere;
import de.grogra.imp3d.objects.TextLabel;
import de.grogra.imp3d.objects.Transformation;
import de.grogra.imp3d.ray2.Raytracer;
import de.grogra.imp3d.shading.MaterialRef;
import de.grogra.imp3d.shading.RGBAShader;
import de.grogra.imp3d.shading.Shader;
import de.grogra.imp3d.shading.ShaderRef;
import de.grogra.math.BSplineCurve;
import de.grogra.math.BSplineCurveList;
import de.grogra.math.BSplineSurface;
import de.grogra.math.TMatrix4d;
import de.grogra.math.TVector3d;
import de.grogra.math.Transform3D;
import de.grogra.math.convexhull.JarvisConvexHull2D;
import de.grogra.math.convexhull.QuickHull3D;
import de.grogra.persistence.Transaction;
import de.grogra.pf.data.DatasetRef;
import de.grogra.pf.io.IO;
import de.grogra.pf.io.ObjectSourceImpl;
import de.grogra.pf.registry.MethodDescriptionContent;
import de.grogra.pf.ui.ChartPanel;
import de.grogra.pf.ui.Console;
import de.grogra.pf.ui.UIProperty;
import de.grogra.pf.ui.Workbench;
import de.grogra.pf.ui.edit.GraphSelection;
import de.grogra.pf.ui.registry.PanelFactory;
import de.grogra.rgg.model.PropertyQueue;
import de.grogra.rgg.model.RGGGraph;
import de.grogra.rgg.model.RGGProducer;
import de.grogra.rgg.model.Runtime;
import de.grogra.turtle.Tropism;
import de.grogra.util.I18NBundle;
import de.grogra.util.Map;
import de.grogra.util.MimeType;
import de.grogra.util.WrapException;
import de.grogra.vecmath.Math2;
import de.grogra.vecmath.Matrix34d;
import de.grogra.vecmath.geom.Cone;
import de.grogra.vecmath.geom.Intersection;
import de.grogra.vecmath.geom.IntersectionList;
import de.grogra.vecmath.geom.Line;
import de.grogra.vecmath.geom.Volume;
import de.grogra.xl.expr.FieldUpdater;
import de.grogra.xl.impl.base.GraphQueue;
import de.grogra.xl.lang.Aggregate;
import de.grogra.xl.lang.DisposableIterator;
import de.grogra.xl.lang.DoubleConsumer;
import de.grogra.xl.lang.DoubleToDouble;
import de.grogra.xl.lang.FloatToFloat;
import de.grogra.xl.lang.IntToDouble;
import de.grogra.xl.lang.ObjectConsumer;
import de.grogra.xl.lang.ObjectToBoolean;
import de.grogra.xl.lang.ObjectToByte;
import de.grogra.xl.lang.ObjectToChar;
import de.grogra.xl.lang.ObjectToDouble;
import de.grogra.xl.lang.ObjectToFloat;
import de.grogra.xl.lang.ObjectToInt;
import de.grogra.xl.lang.ObjectToLong;
import de.grogra.xl.lang.ObjectToObject;
import de.grogra.xl.lang.ObjectToObjectGenerator;
import de.grogra.xl.lang.ObjectToShort;
import de.grogra.xl.util.BooleanList;
import de.grogra.xl.util.ByteList;
import de.grogra.xl.util.CharList;
import de.grogra.xl.util.DoubleList;
import de.grogra.xl.util.FloatList;
import de.grogra.xl.util.IntList;
import de.grogra.xl.util.LongList;
import de.grogra.xl.util.ObjectList;
import de.grogra.xl.util.Operators;
import de.grogra.xl.util.ShortList;
import de.lmu.ifi.dbs.elki.data.spatial.Polygon;
import de.lmu.ifi.dbs.elki.math.geometry.AlphaShape;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * This class contains a collection of methods and constants which are
 * useful in RGG modelling, especially in 3D.
 * 
 * @author Ole Kniemeyer
 */
public class Library
{
	/**
	 * This is a redefinition of {@link Graph#SUCCESSOR_EDGE}.
	 */
	public static final int successor = Graph.SUCCESSOR_EDGE;

	/**
	 * This is a redefinition of {@link Graph#BRANCH_EDGE}.
	 */
	public static final int branch = Graph.BRANCH_EDGE;

	/**
	 * This is a redefinition of {@link Graph#CONTAINMENT_EDGE}.
	 */
	public static final int contains = Graph.CONTAINMENT_EDGE;

	/**
	 * This is a redefinition of {@link Graph#REFINEMENT_EDGE}.
	 */
	public static final int refine = Graph.REFINEMENT_EDGE;

	/**
	 * This is a redefinition of {@link Graph#NOTIFIES_EDGE}.
	 */
	public static final int notifies = Graph.NOTIFIES_EDGE;

	/**
	 * This is a redefinition of {@link Graph#MARK_EDGE}.
	 */
	public static final int mark = Graph.MARK_EDGE;

	/**
	 * This is a redefinition of {@link FieldUpdater#UPDATES}.
	 */
	public static final int updates = FieldUpdater.UPDATES;

	/**
	 * This is a redefinition of {@link Instance3D#MASTER}.
	 */
	public static final int master = Instance3D.MASTER;

	public static final int EDGE_0 = Graph.MIN_UNUSED_EDGE << 0;
	public static final int EDGE_1 = Graph.MIN_UNUSED_EDGE << 1;
	public static final int EDGE_2 = Graph.MIN_UNUSED_EDGE << 2;
	public static final int EDGE_3 = Graph.MIN_UNUSED_EDGE << 3;
	public static final int EDGE_4 = Graph.MIN_UNUSED_EDGE << 4;
	public static final int EDGE_5 = Graph.MIN_UNUSED_EDGE << 5;
	public static final int EDGE_6 = Graph.MIN_UNUSED_EDGE << 6;
	public static final int EDGE_7 = Graph.MIN_UNUSED_EDGE << 7;
	public static final int EDGE_8 = Graph.MIN_UNUSED_EDGE << 8;
	public static final int EDGE_9 = Graph.MIN_UNUSED_EDGE << 9;
	public static final int EDGE_10 = Graph.MIN_UNUSED_EDGE << 10;
	public static final int EDGE_11 = Graph.MIN_UNUSED_EDGE << 11;
	public static final int EDGE_12 = Graph.MIN_UNUSED_EDGE << 12;
	public static final int EDGE_13 = Graph.MIN_UNUSED_EDGE << 13;
	public static final int EDGE_14 = Graph.MIN_UNUSED_EDGE << 14;

	static
	{
		assert EDGE_14 == Integer.MIN_VALUE;
	}

	/**
	 * Constant for {@link NURBSSurface#NURBSSurface(byte)} indicating a
	 * skinned surface (defined by a set of profile curves in space).
	 */
	public static final byte SKIN = NURBSSurface.SKIN;

	/**
	 * Constant for {@link NURBSSurface#NURBSSurface(byte)} indicating a
	 * swept surface (defined by a set of vertices in space).
	 */
	public static final byte SWEEP = NURBSSurface.SWEEP;

	/**
	 * The resource bundle for the <code>de.grogra.rgg</code> package.
	 */
	public static final I18NBundle I18N = I18NBundle
		.getInstance (Library.class);

	/**
	 * Conversion factor from degree to radians.
	 */
	public static final double DEG = Math.PI / 180;

	/**
	 * Conversion factor from radians to degree.
	 */
	public static final double R2D = 180 / Math.PI;

	/**
	 * Vector pointing in the x-direction (the turtle's left axis).
	 * Do not modify its contents.
	 */
	public static final Vector3d LEFT = new Vector3d (1, 0, 0);

	/**
	 * Vector pointing in the y-direction (the turtle's up axis).
	 * Do not modify its contents.
	 */
	public static final Vector3d UP = new Vector3d (0, 1, 0);

	/**
	 * Vector pointing in the z-direction (the turtle's head axis).
	 * Do not modify its contents.
	 */
	public static final Vector3d HEAD = new Vector3d (0, 0, 1);

	/**
	 * Point having coordinates (0, 0, 0).
	 * Do not modify its contents.
	 */
	public static final Point3d ORIGIN = new Point3d (0, 0, 0);
	
	public static final RGGProducer.Creator makeGraph = new RGGProducer.Creator ();

	public final static int CONVEXHULL = 1;
	public final static int BOUNDING_RECTANGLE = 0;
	public final static int ALPHA_SHAPE = 2;

	// stopwatch timer stack
	private static Stack<Long> stopwatchStack = new Stack<Long>();
	
	/**
	 * Mapping between the target format keys and the identification strings of the mime types.
	 * Only used in export3DScene.
	 */
	private static final HashMap<String, String> EXPORT_FORMAT_MAP = createMap();

	private static HashMap<String, String> createMap() {
		HashMap<String, String> result = new HashMap<String, String>();
		result.put("dxf", "image/vnd.dxf");
		//result.put("obj", "image/vnd.obj"); //not working
		result.put("x3d", "model/vnd.x3d");
		result.put("vrml97", "model/vrml"); //not working
		//result.put("classicvrml", "model/x3d+vrml");  //not working
		result.put("mtg", "model/mtg");
		result.put("stl", "model/stl");
		result.put("webgl", "model/webgl");
		return result;
		//return Collections.unmodifiableMap(result);
	}

	//coordinates of some default leaf shapes
	public static float[][] DEFAULT_LEAF3D = new float[][] {
		// first leaf
		{
			0, 0, 0, // point zero: x,y,z
			-0.01f, 0.01f, 0, // point one: x,y,z
			-0.0075f, 0.0325f, 0.005f, // point two: x,y,z
			0.01f, 0.06f, 0.0025f, //...
			0.02f, 0.04f, 0.005f,
			0.025f, 0.02f, 0.0025f,
			0.015f, 0.005f, 0
		},
		//some kind of maple
		{
			 0.000f, 0.000f, 0.000f, 0.010f,-0.020f,-0.005f, 0.020f,-0.030f, 0.000f, 0.040f,-0.040f, 0.005f, 0.050f,-0.030f, 0.000f,
			 0.070f,-0.040f,-0.005f, 0.080f,-0.025f, 0.000f, 0.100f,-0.005f, 0.005f, 0.090f, 0.010f, 0.000f, 0.100f, 0.025f,-0.005f,
			 0.090f, 0.035f, 0.000f, 0.100f, 0.080f, 0.005f, 0.070f, 0.080f, 0.000f, 0.050f, 0.080f,-0.005f, 0.050f, 0.100f, 0.000f,
			 0.040f, 0.090f, 0.005f, 0.030f, 0.120f, 0.000f, 0.020f, 0.110f,-0.005f, 0.000f, 0.145f, 0.005f,-0.020f, 0.110f,-0.005f,
			-0.030f, 0.120f, 0.000f,-0.040f, 0.090f, 0.005f,-0.050f, 0.100f, 0.000f,-0.050f, 0.080f,-0.005f,-0.070f, 0.080f, 0.000f,
			-0.100f, 0.080f, 0.005f,-0.090f, 0.035f, 0.000f,-0.100f, 0.025f,-0.005f,-0.090f, 0.010f, 0.000f,-0.100f,-0.005f, 0.005f,
			-0.080f,-0.025f, 0.000f,-0.070f,-0.040f,-0.005f,-0.050f,-0.030f, 0.000f,-0.040f,-0.040f, 0.005f,-0.020f,-0.030f, 0.000f,
			-0.010f,-0.020f,-0.005f
		},
		//
		{
			 0.000f, 0.000f, 0.000f, 0.010f, 0.005f, 0.0062f, 0.030f, 0.015f, 0.0124f, 0.055f, 0.035f, 0.0183f, 0.075f, 0.055f, 0.0240f,
			 0.090f, 0.075f, 0.0293f, 0.100f, 0.095f, 0.0341f, 0.100f, 0.115f, 0.0384f, 0.095f, 0.145f, 0.0421f, 0.090f, 0.165f, 0.0451f,
			 0.070f, 0.195f, 0.0474f, 0.060f, 0.215f, 0.0490f, 0.040f, 0.230f, 0.0499f, 0.020f, 0.255f, 0.0499f, 0.010f, 0.275f, 0.0492f,
			 0.005f, 0.295f, 0.0492f, 0.000f, 0.325f, 0.0455f,-0.005f, 0.295f, 0.0477f,-0.010f, 0.275f, 0.0492f,-0.020f, 0.255f, 0.0499f,
			-0.040f, 0.235f, 0.0499f,-0.060f, 0.210f, 0.0490f,-0.070f, 0.195f, 0.0474f,-0.090f, 0.165f, 0.0451f,-0.100f, 0.145f, 0.0421f,
			-0.105f, 0.115f, 0.0384f,-0.105f, 0.095f, 0.0341f,-0.100f, 0.075f, 0.0293f,-0.090f, 0.055f, 0.0240f,-0.070f, 0.035f, 0.0183f,
			-0.040f, 0.015f, 0.0124f,-0.020f, 0.005f, 0.0062f},
		//poplar
		{
			0,0.01f,0, 0.02f,-0.005f,0.0164f, 0.04f,-0.01f,0.0309f, 0.06f,-0.01f,0.0421f, 0.09f,0,0.0486f, 0.105f,0.015f,0.0498f, 0.115f,0.03f,0.0455f, 0.115f,0.075f,0.0361f, 0.11f,0.11f,0.0229f, 0.10f,0.14f,0.0071f, 0.07f,0.17f,-0.0095f, 0.05f,0.19f,-0.0251f, 0.03f,0.20f,-0.0378f, 0.01f,0.21f,-0.0465f, 
			0,0.24f,-0.0510f,//top
			-0.01f,0.215f,-0.0499f, -0.04f,0.20f,-0.0465f, -0.06f,0.185f,-0.0378f, -0.075f,0.17f,-0.0251f, -0.10f,0.14f,-0.0095f, -0.11f,0.11f,0.0071f, -0.12f,0.075f,0.0229f, -0.125f,0.03f,0.0361f, -0.11f,0.01f,0.0455f, -0.095f,0,0.0498f, -0.07f,-0.01f,0.0486f, -0.05f,-0.01f,0.0421f, -0.03f,-0.005f,0.0309f, -0.0125f,0,0.0164f
		},
		//cotton
		{
			//75% of the "original" data
			0.000000000f, 0.000000000f, 0.000000000f, 0.013333333f,-0.020000000f, 0.000000000f, 0.026666667f,-0.026666667f, 0.000000000f, 0.040000000f,
			-0.026666667f, 0.000000000f, 0.053333333f,-0.026666667f, 0.000000000f, 0.066666667f,-0.024000000f, 0.000000000f, 0.080000000f,-0.022666667f,
			 0.000000000f, 0.093333333f,-0.016000000f, 0.000000000f, 0.106666667f,-0.006666667f, 0.000000000f, 0.117333333f, 0.020000000f, 0.000000000f,
			 0.125333333f, 0.053333333f, 0.000000000f, 0.126666667f, 0.080000000f, 0.000000000f, 0.130666667f, 0.106666667f, 0.000000000f, 0.142666667f,
			 0.133333333f, 0.000000000f, 0.148000000f, 0.146666667f, 0.000000000f, 0.133333333f, 0.137333333f, 0.000000000f, 0.120000000f, 0.130666667f,
			 0.000000000f, 0.106666667f, 0.129333333f, 0.000000000f, 0.093333333f, 0.128000000f, 0.000000000f, 0.080000000f, 0.122666667f, 0.000000000f,
			 0.066666667f, 0.117333333f, 0.000000000f, 0.057333333f, 0.113333333f, 0.000000000f, 0.057333333f, 0.140000000f, 0.000000000f, 0.053333333f,
			 0.160000000f, 0.000000000f, 0.049333333f, 0.173333333f, 0.000000000f, 0.044000000f, 0.186666667f, 0.000000000f, 0.037333333f, 0.200000000f,
			 0.000000000f, 0.026666667f, 0.213333333f, 0.000000000f, 0.016000000f, 0.233333333f, 0.000000000f, 0.013333333f, 0.253333333f, 0.000000000f,
			 0.013333333f, 0.280000000f, 0.000000000f, 0.000000000f, 0.265333333f, 0.000000000f,-0.006666667f, 0.250666667f, 0.000000000f,-0.013333333f,
			 0.237333333f, 0.000000000f,-0.025333333f, 0.226666667f, 0.000000000f,-0.040000000f, 0.206666667f, 0.000000000f,-0.053333333f, 0.186666667f,
			 0.000000000f,-0.060000000f, 0.160000000f, 0.000000000f,-0.066666667f, 0.132000000f, 0.000000000f,-0.080000000f, 0.125333333f, 0.000000000f,
			-0.093333333f, 0.130666667f, 0.000000000f,-0.106666667f, 0.134666667f, 0.000000000f,-0.120000000f, 0.137333333f, 0.000000000f,-0.133333333f,
			 0.136000000f, 0.000000000f,-0.146666667f, 0.138666667f, 0.000000000f,-0.160000000f, 0.144000000f, 0.000000000f,-0.176000000f, 0.150666667f,
			 0.000000000f,-0.160000000f, 0.129333333f, 0.000000000f,-0.152000000f, 0.106666667f, 0.000000000f,-0.145333333f, 0.080000000f, 0.000000000f,
			-0.137333333f, 0.053333333f, 0.000000000f,-0.132000000f, 0.040000000f, 0.000000000f,-0.124000000f, 0.020000000f, 0.000000000f,-0.116000000f,
			 0.012000000f, 0.000000000f,-0.109333333f, 0.000000000f, 0.000000000f,-0.105333333f,-0.013333333f, 0.000000000f,-0.093333333f,-0.022666667f,
			 0.000000000f,-0.080000000f,-0.029333333f, 0.000000000f,-0.066666667f,-0.032000000f, 0.000000000f,-0.053333333f,-0.033333333f, 0.000000000f,
			-0.040000000f,-0.030666667f, 0.000000000f,-0.026666667f,-0.024000000f, 0.000000000f,-0.013333333f,-0.001666667f, 0.000000000f
			//75% of the "original" data
//			0,0,0, 1,-1.5f,0, 2,-2,0, 3,-2,0, 4,-2,0, 5,-1.8f,0, 6,-1.7f,0, 7,-1.2f,0, 8,-0.5f,0, 8.8f,1.5f,0, 9.4f,4,0, 9.5f,6,0, 9.8f,8,0, 10.7f,10,0, 11.1f,11,0, 10,10.3f,0, 9,9.8f,0, 8,9.7f,0, 7,9.6f,0, 6,9.2f,0, 5,8.8f,0, 4.3f,8.5f,0, 4.3f,10.5f,0, 4,12,0, 3.7f,13,0, 3.3f,14,0, 2.8f,15,0, 2,16,0, 1.2f,17.5f,0, 1,19,0, 1,21,0, 
//			0,19.9f,0, //top
//			-0.5f,18.8f,0, -1,17.8f,0, -1.9f,17,0, -3,15.5f,0, -4,14,0, -4.5f,12,0, -5,9.9f,0, -6,9.4f,0, -7,9.8f,0, -8,10.1f,0, -9,10.3f,0, -10,10.2f,0, -11,10.4f,0, -12,10.8f,0, -13.2f,11.3f,0, -12,9.7f,0, -11.4f,8,0, -10.9f,6,0, -10.3f,4,0, -9.9f,3,0, -9.3f,1.5f,0, -8.7f,0.9f,0, -8.2f,0,0, -7.9f,-1,0, -7,-1.7f,0, -6,-2.2f,0, -5,-2.4f,0, -4,-2.5f,0, -3,-2.3f,0, -2,-1.8f,0, -1,-0.5f,0
		},
		//birch
		{
			0,0,0, 0.01f,0.005f,0, 0.02f,0.007f,0, 0.03f,0.01f,0, 0.04f,0.015f,0, 0.05f,0.018f,0, 0.06f,0.022f,0, 0.07f,0.028f,0, 0.072f,0.031f,0, 0.08f,0.04f,0, 0.085f,0.05f,0, 0.088f,0.07f,0, 0.082f,0.07f,0, 0.084f,0.082f,0, 0.08f,0.081f,0, 0.083f,0.093f,0, 0.083f,0.103f,0, 0.08f,0.118f,0, 0.07f,0.116f,0, 0.067f,0.122f,0, 0.064f,0.121f,0, 0.063f,0.131f,0, 0.064f,0.141f,0, 0.059f,0.15f,0, 0.051f,0.148f,0, 0.049f,0.155f,0, 0.046f,0.153f,0, 0.041f,0.175f,0, 0.032f,0.171f,0, 0.026f,0.191f,0, 0.021f,0.188f,0, 0.017f,0.206f,0, 0.012f,0.205f,0, 0.01f,0.215f,0, 0.011f,0.222f,0, 0.008f,0.221f,0, 0.005f,0.23f,0, 0.005f,0.235f,0, 
			-0.002f,0.25f,0,//top
			-0.002f,0.226f,0, -0.005f,0.222f,0, -0.007f,0.21f,0, -0.011f,0.211f,0, -0.019f,0.196f,0, -0.024f,0.199f,0, -0.03f,0.18f,0, -0.035f,0.174f,0, -0.043f,0.178f,0, -0.05f,0.161f,0, -0.055f,0.154f,0, -0.063f,0.156f,0, -0.067f, 0.148f,0, -0.067f,0.14f,0, -0.07f,0.141f,0, -0.07f,0.128f,0, -0.074f,0.128f,0, -0.074f,0.12f,0, -0.084f,0.12f,0, -0.085f,0.109f,0, -0.087f,0.10f,0, -0.086f,0.09f,0, -0.089f,0.088f,0, -0.088f,0.08f,0, -0.095f,0.078f,0, -0.094f,0.07f,0, -0.093f,0.062f,0, -0.093f,0.056f,0, -0.084f,0.047f,0, -0.087f,0.044f,0, -0.074f,0.033f,0, -0.077f,0.03f,0, -0.07f,0.025f,0, -0.06f,0.022f,0, -0.05f,0.019f,0, -0.04f,0.013f,0, -0.03f,0.009f,0, -0.02f,0.005f,0, -0.01f,0.003f,0
		},
		//
		{
			0,0,0, 0.01f,0.005f,0.035f, 0.02f,0.01f,0.069f, 0.03f,0.02f,0.11f, 0.04f,0.03f,0.138f, 0.05f,0.045f,0.15f, 0.06f,0.062f,0.182f, 0.07f,0.095f,0.225f, 0.074f,0.12f,0.248f, 0.076f,0.14f,0.264f, 0.075f,0.16f,0.277f, 0.07f,0.195f,0.297f, 0.065f,0.215f,0.307f, 0.05f,0.253f,0.323f, 0.037f,0.28f,0.333f, 0.02f,0.31f,0.344f, 
			0,0.34f,0.353f, //top
			-0.02f,0.31f,0.344f, -0.037f,0.28f,0.333f, -0.05f,0.253f,0.323f, -0.065f,0.215f,0.307f, -0.07f,0.195f,0.297f, -0.075f,0.16f,0.277f, -0.076f,0.14f,0.264f, -0.074f,0.12f,0.248f, -0.07f,0.095f,0.225f, -0.06f,0.062f,0.182f, -0.05f,0.045f,0.15f, -0.04f,0.03f,0.138f, -0.03f,0.02f,0.109f, -0.02f,0.01f,0.069f, -0.01f,0.005f,0.035f
		},
		//oak
		{
			 0.0000f, 0.0900f, 0.0000f, 0.0000f, 0.0000f, 0.0000f, 0.0050f, 0.0125f, 0.0000f, 0.0100f, 0.0200f, 0.0000f, 0.0150f,
			 0.0300f, 0.0000f, 0.0125f, 0.0400f, 0.0000f, 0.0250f, 0.0500f, 0.0000f, 0.0300f, 0.0550f, 0.0000f, 0.0300f, 0.0600f,
			 0.0000f, 0.0250f, 0.0650f, 0.0000f, 0.0300f, 0.0700f, 0.0000f, 0.0350f, 0.0750f, 0.0000f, 0.0400f, 0.0800f, 0.0000f,
			 0.0450f, 0.0900f, 0.0000f, 0.0475f, 0.0950f, 0.0000f, 0.0450f, 0.1000f, 0.0000f, 0.0400f, 0.1000f, 0.0000f, 0.0275f,
			 0.0975f, 0.0000f, 0.0300f, 0.1000f, 0.0000f, 0.0400f, 0.1050f, 0.0000f, 0.0450f, 0.1100f, 0.0000f, 0.0500f, 0.1150f,
			 0.0000f, 0.0500f, 0.1325f, 0.0000f, 0.0450f, 0.1375f, 0.0000f, 0.0350f, 0.1400f, 0.0000f, 0.0250f, 0.1350f, 0.0000f,
			 0.0250f, 0.1400f, 0.0000f, 0.0300f, 0.1500f, 0.0000f, 0.0275f, 0.1600f, 0.0000f, 0.0200f, 0.1625f, 0.0000f, 0.0150f,
			 0.1650f, 0.0000f, 0.0100f, 0.1700f, 0.0000f, 0.0050f, 0.1725f, 0.0000f, 0.0000f, 0.1700f, 0.0000f,-0.0050f, 0.1675f,
			 0.0000f,-0.0100f, 0.1680f, 0.0000f,-0.0150f, 0.1675f, 0.0000f,-0.0200f, 0.1540f, 0.0000f,-0.0250f, 0.1560f, 0.0000f,
			-0.0300f, 0.1580f, 0.0000f,-0.0350f, 0.1595f, 0.0000f,-0.0400f, 0.1570f, 0.0000f,-0.0450f, 0.1450f, 0.0000f,-0.0400f,
			 0.1295f, 0.0000f,-0.0350f, 0.1200f, 0.0000f,-0.0400f, 0.1205f, 0.0000f,-0.0450f, 0.1220f, 0.0000f,-0.0500f, 0.1230f,
			 0.0000f,-0.0550f, 0.1220f, 0.0000f,-0.0600f, 0.1150f, 0.0000f,-0.0550f, 0.0965f, 0.0000f,-0.0500f, 0.0900f, 0.0000f,
			-0.0450f, 0.0855f, 0.0000f,-0.0400f, 0.0825f, 0.0000f,-0.0350f, 0.0795f, 0.0000f,-0.0300f, 0.0765f, 0.0000f,-0.0325f,
			 0.0740f, 0.0000f,-0.0350f, 0.0745f, 0.0000f,-0.0400f, 0.0740f, 0.0000f,-0.0450f, 0.0720f, 0.0000f,-0.0460f, 0.0675f,
			 0.0000f,-0.0450f, 0.0625f, 0.0000f,-0.0400f, 0.0550f, 0.0000f,-0.0350f, 0.0500f, 0.0000f,-0.0300f, 0.0475f, 0.0000f,
			-0.0250f, 0.0450f, 0.0000f,-0.0200f, 0.0400f, 0.0000f,-0.0245f, 0.0300f, 0.0000f,-0.0200f, 0.0250f, 0.0000f,-0.0150f,
			 0.0180f, 0.0000f,-0.0100f, 0.0110f, 0.0000f,-0.0050f, 0.0050f, 0.0000f, 0.0000f, 0.0000f, 0.0000f
		}
	};
	//species according to the default leaf shapes (DEFAULT_LEAF3D)
	public static String[] DEFAULT_LEAF3D_NAME = new String[] {"artificial", "maple", "undefined", "poplar", "cotton", "birch", "undefined", "oak"};


	private static final class Locals
	{
		final Line line = new Line ();
		final IntersectionList ilist = new IntersectionList ();

		Locals ()
		{
		}
	}

	private static final ThreadLocal<Locals> LOCALS = new ThreadLocal<Locals> ();

	private static Locals currentLocals ()
	{
		Locals loc = LOCALS.get ();
		if (loc == null)
		{
			loc = new Locals ();
			LOCALS.set (loc);
		}
		return loc;
	}

	/**
	 * This field provides a <code>PrintWriter</code> to write
	 * to the XL console.
	 */
	public static final PrintWriter out = new PrintWriter (new Writer ()
	{
		@Override
		public void write (char[] cbuf, int off, int len)
		{
			Console c = console ();
			if (c != null)
			{
				c.getOut ().write (cbuf, off, len);
				c.getOut ().flush ();
			}
			else
			{
				if ((off != 0) || (len != cbuf.length))
				{
					System.arraycopy (cbuf, off, cbuf = new char[len], 0, len);
				}
				System.out.print (cbuf);
			}
		}

		@Override
		public void flush ()
		{
			Console c = console ();
			if (c != null)
			{
				c.getOut ().flush ();
			}
			else
			{
				System.out.flush ();
			}
		}

		@Override
		public void close ()
		{
		}
	});

	/**
	 * Returns the currently active <code>Console</code>. This may return
	 * <code>null</code>. 
	 * 
	 * @return current console or <code>null</code>
	 */
	public static Console console ()
	{
		Workbench w = Workbench.current ();
		if (w != null)
		{
			return (Console) PanelFactory.getAndShowPanel (w,
				"/ui/panels/rgg/console", Map.EMPTY_MAP);
		}
		return null;
	}

	/**
	 * Returns the currently active <code>Workbench</code>. This may return
	 * <code>null</code>. 
	 * 
	 * @return current workbench or <code>null</code>
	 */
	public static IMPWorkbench workbench ()
	{
		return (IMPWorkbench) Workbench.current ();
	}

	/**
	 * Closes the currently active <code>Workbench</code> if such a workbench
	 * exists. Note that unsaved modifications are lost, because the user is
	 * not asked. 
	 */
	public static void closeWorkbench ()
	{
		Workbench w = workbench ();
		if (w != null)
		{
			w.ignoreIfModified ();
			IMP.closeWorkbench (w);
		}
	}

	/**
	 * Returns the current project graph, i.e., the project graph of the
	 * currently active <code>Workbench</code>. This may return
	 * <code>null</code>. 
	 * 
	 * @return current project graph or <code>null</code>
	 */
	public static GraphManager graph ()
	{
		IMPWorkbench w = workbench ();
		return (w != null) ? w.getRegistry ().getProjectGraph () : null;
	}

	/**
	 * Returns the current project graph, i.e., the project graph of the
	 * currently active <code>Workbench</code>. This may return
	 * <code>null</code>. 
	 * 
	 * @return current project graph or <code>null</code>
	 */
	public static GraphManager getProjectGraph ()
	{
		IMPWorkbench w = workbench ();
		return (w != null) ? w.getRegistry ().getProjectGraph () : null;
	}	

	/**
	 * Returns the current meta/registry graph, i.e., the meta/registry graph of the
	 * currently active <code>Workbench</code>. This may return
	 * <code>null</code>. 
	 * 
	 * @return current meta/registry graph or <code>null</code>
	 */
	public static GraphManager getMetaGraph ()
	{
		IMPWorkbench w = workbench ();
		return (w != null) ? w.getRegistry ().getRegistryGraph () : null;
	}
	
	/**
	 * Returns the current graph state, i.e., the current graph state of the
	 * project graph of the currently active <code>Workbench</code>.
	 * 
	 * @return current graph state
	 */
	public static GraphState graphState ()
	{
		return GraphState.current (graph ());
	}

	@Deprecated
	public static RGGGraph extent ()
	{
		return rggGraph ();
	}

	public static RGGGraph rggGraph ()
	{
		return Runtime.INSTANCE.currentGraph ();
	}

	public static void allowNoninjectiveMatchesByDefault (boolean value)
	{
		rggGraph ().allowNoninjectiveMatchesByDefault (value);
	}

	@Deprecated
	public static void allowNoninjectiveMatchesForNextQuery (boolean value)
	{
		rggGraph ().allowNoninjectiveMatchesForNextQuery (value);
	}

	public static void setDerivationMode (int mode)
	{
		rggGraph ().setDerivationMode (mode);
	}


	/**
	 * Start stopwatch timer
	 * 
	 * tic starts a stopwatch timer to measure performance. The function records the internal time 
	 * at execution of the tic command. Display the elapsed time with the toc function.
	 * 
	 * @return returns the value of the internal timer [ms] at the execution of the tic command
	 */
	public static long tic() {
		long tmp = System.currentTimeMillis();
		stopwatchStack.push(tmp);
		return tmp;
	}
	
	/**
	 * Read elapsed time from stopwatch
	 * 
	 * toc reads the elapsed time from the stopwatch timer started by the tic function. 
	 * The function reads the internal time at the execution of the toc command, and displays 
	 * the elapsed time since the most recent call to the tic function, in milliseconds.
	 * 
	 * @return returns the elapsed time; -1, if tic was not called in advance
	 */
	public static long toc() {
		if (!stopwatchStack.empty()) {
			// remove the last entry from the stack and return the difference to the current system time
			return System.currentTimeMillis()-stopwatchStack.pop();
		}
		return -1;
	}

	/**
	 * Undo the last derivation step.
	 * 
	 */
	public static void undo () {
		workbench ().undo ();
	}

	/**
	 * Returns a node having the specified <code>name</code>.
	 * 
	 * @param name name of a node
	 * @return a node whose name is <code>name</code>, or <code>null</code>
	 * if no such node exists
	 * 
	 * @see GraphManager#getNodeForName
	 */
	public static Node namedNode (String name)
	{
		return graph ().getNodeForName (name);
	}

	/**
	 * Displays <code>text</code> in the current status bar. 
	 * 
	 * @param text a text to be displayed
	 */
	public static void setStatus (String text)
	{
		Workbench w = Workbench.current ();
		if (w != null)
		{
			w.beginStatus (null);
			w.setStatus (null, text);
		}
	}

	/**
	 * Shows the data referenced by <code>ds</code> in a chart panel.
	 * <code>type</code> is one of the constants declared in
	 * {@link ChartPanel}, this specifies the type of chart.
	 * 
	 * @param ds reference to a {@link de.grogra.pf.data.Dataset}
	 * @param type chart type
	 */
	public static void chart (DatasetRef ds, int type)
	{
		ChartPanel cp = workbench ().getChartPanel (ds.getName (), null);
		if (cp != null)
		{
			cp.setChart (ds.resolve (), type, null);
			cp.show (false, null);
		}
	}

	public static Node axisparent (Node child)
	{
		return child.getAxisParent ();
	}

	/**
	 * Tests whether <code>a</code> is an ancestor of <code>d</code>
	 * (or <code>d</code> itself), i.e., if <code>a</code> can be reached
	 * from <code>d</code> by a backward traversal of edges matching
	 * <code>edgeMask</code> (see {@link Edge#testEdgeBits(int)}).
	 * Note that this method expects the relevant part of the subgraph
	 * spanned by matching edges to be a tree.
	 * 
	 * @param a the potential ancestor
	 * @param d the potential descendant
	 * @param edgeMask the edge mask to use
	 * @return <code>true</code> iff <code>a</code> is an ancestor
	 * of <code>d</code>
	 */
	public static boolean isAncestor (Node a, Node d, int edgeMask)
	{
		ascend: while (d != a)
		{
			for (Edge e = d.getFirstEdge (); e != null; e = e.getNext (d))
			{
				Node s;
				if ((s = e.getSource ()) != d)
				{
					if (e.testEdgeBits (edgeMask))
					{
						d = s;
						continue ascend;
					}
				}
			}
			return false;
		}
		return true;

	}

	/**
	 * Returns a uniformly distributed integral pseudorandom number
	 * between <code>min</code> and <code>max</code> (inclusive).
	 * 
	 * @param min minimal value
	 * @param max maximal value
	 * @return integral random number between <code>min</code> and <code>max</code>
	 * 
	 * @see #setSeed(long)
	 */
	public static int irandom (int min, int max)
	{
		return Operators.getRandomGenerator ().nextInt (max + 1 - min) + min;
	}

	/**
	 * Returns a uniformly distributed pseudorandom number
	 * between <code>min</code> and <code>max</code>.
	 * 
	 * @param min minimal value
	 * @param max maximal value
	 * @return random number between <code>min</code> and <code>max</code>
	 * 
	 * @see #setSeed(long)
	 */
	public static float random (float min, float max)
	{
		return Operators.getRandomGenerator ().nextFloat () * (max - min) + min;
	}

	/**
	 * Returns the value of a <code>boolean</code> pseudorandom variable
	 * whose probability for <code>true</code> is <code>p</code>.
	 * 
	 * @param p probability for <code>true</code>
	 * @return random value
	 * 
	 * @see #setSeed(long)
	 */
	public static boolean probability (float p)
	{
		return (p >= 1) || (Operators.getRandomGenerator ().nextFloat () < p);
	}

	/**
	 * Returns a pseudorandom number which is distributed according
	 * to a normal distribution with mean value <code>mu</code> and standard
	 * deviation <code>sigma</code>.
	 * 
	 * @param mu mean value
	 * @param sigma standard deviation
	 * @return normally distributed random number
	 * 
	 * @see #setSeed(long)
	 */
	public static float normal (float mu, float sigma)
	{
		return mu + sigma * (float) Operators.getRandomGenerator ().nextGaussian ();
	}

	public static double lognormal (double mu, double sigma)
	{
		double t = sigma / mu;
		double s2 = Math.log1p (t * t);
		return mu
			* Math.exp (-0.5 * s2 + Math.sqrt (s2)
				* Operators.getRandomGenerator ().nextGaussian ());
	}

	/**
	 * Returns a pseudorandom number which is distributed according
	 * to a normal distribution with mean value <code>mu</code> and standard
	 * deviation <code>sigma</code>.
	 * 
	 * @param mu mean value
	 * @param sigma standard deviation
	 * @return normally distributed random number
	 * 
	 * @see #setSeed(long)
	 */
	public static double normal (double mu, double sigma)
	{
		return mu + sigma * Operators.getRandomGenerator ().nextGaussian ();
	}

	/**
	 * Returns an integral pseudorandom number according to the
	 * discrete distribution <code>probabilities</code>. The
	 * value <code>i</code> has the probability
	 * <code>probabilities[i]</code> for
	 * <code>0 <= i < probabilities.length</code>, the
	 * value <code>probabilities.length</code> has the
	 * remaining probability, i.e., the difference between
	 * the sum of <code>probabilities</code> and <code>1</code>.
	 * 
	 * @param probabilities array of discrete probabilities
	 * @return random number according to <code>probabilities</code>
	 * 
	 * @see #setSeed(long)
	 */
	public static int distribution (float[] probabilities)
	{
		float f = Operators.getRandomGenerator ().nextFloat ();
		int n = probabilities.length - 1;
		for (int i = 0; i < n; i++)
		{
			if ((f -= probabilities[i]) <= 0)
			{
				return i;
			}
		}
		return n;
	}

	/**
	 * This method sets the seed for the pseudorandom number generator
	 * which is used by the random methods in this class. This is the
	 * generator obtained by {@link Operators#getRandomGenerator()}
	 * within the current thread.
	 * 
	 * @param seed a seed
	 */
	public static void setSeed (long seed)
	{
		Operators.getRandomGenerator ().setSeed (seed);
	}


	/**
	 * Returns the nearest ancestor of <code>n</code> which is an instance
	 * of <code>t</code>. Ancestors are those nodes which can be reached by
	 * traversing {@link #successor}- or {@link #branch}-edges backwards.
	 * 
	 * @param n a node
	 * @param t the type so search for
	 * @return nearest ancestor of type <code>t</code>, or <code>null</code>
	 */
	public static <C extends Node> C ancestor (Node n, Class<C> t)
	{
		while (true)
		{
			Node m = null;
			for (Edge e = n.getFirstEdge (); e != null; e = e.getNext (n))
			{
				if (e.testEdgeBits (Graph.BRANCH_EDGE | Graph.SUCCESSOR_EDGE)
					&& e.isTarget (n))
				{
					m = e.getSource ();
					break;
				}
			}
			if ((m == null) || t.isInstance (m))
			{
				return t.cast (m);
			}
			n = m;
		}
	}

	/**
	 * This generator method yields all descendants of <code>n</code>
	 * which are instances of <code>t</code> and which have no
	 * other instance of <code>t</code> in their path to <code>n</code>.
	 * Descendants are those nodes which can be reached by
	 * traversing {@link #successor}- or {@link #branch}-edges forwards.
	 * 
	 * @param cb a consumer instance receiving the nodes (provided by the XL compiler)
	 * @param n a node
	 * @param t the type to seach for
	 * @return <code>null</code>
	 */
	public static <T> Node minDescendants (ObjectConsumer<? super T> cb, Node n, Class<T> t)
	{
		for (Edge e = n.getFirstEdge (); e != null; e = e.getNext (n))
		{
			Node m = e.getTarget ();
			if ((n != m)
				&& e.testEdgeBits (Graph.BRANCH_EDGE | Graph.SUCCESSOR_EDGE))
			{
				if (t.isInstance (m))
				{
					cb.consume ((T) m);
				}
				else
				{
					minDescendants (cb, m, t);
				}
			}
		}
		return null;
	}

	/**
	 * Returns the nearest ancestor of <code>n</code> which fulfills
	 * <code>condition</code>. Ancestors are those nodes which can be reached by
	 * traversing {@link #successor}- or {@link #branch}-edges backwards.
	 * 
	 * @param n a node
	 * @param condition a condition
	 * @return nearest ancestor fulfilling <code>condition</code>, or <code>null</code>
	 */
	public static Node ancestor (Node n, ObjectToBoolean<? super Node> condition)
	{
		while (true)
		{
			Node m = null;
			for (Edge e = n.getFirstEdge (); e != null; e = e.getNext (n))
			{
				if (e.testEdgeBits (Graph.BRANCH_EDGE | Graph.SUCCESSOR_EDGE)
					&& e.isTarget (n))
				{
					m = e.getSource ();
					break;
				}
			}
			if ((m == null) || condition.evaluateBoolean (m))
			{
				return m;
			}
			n = m;
		}
	}

	/**
	 * Returns the nearest successor of <code>n</code> which fulfills
	 * <code>condition</code>. Successors are those nodes which can be reached by
	 * traversing {@link #successor}-edges forwards.
	 * 
	 * @param n a node
	 * @param condition a condition
	 * @return nearest successor fulfilling <code>condition</code>, or <code>null</code>
	 */
	public static Node successor (Node n, ObjectToBoolean<? super Node> condition)
	{
		while (true)
		{
			Node m = null;
			for (Edge e = n.getFirstEdge (); e != null; e = e.getNext (n))
			{
				if (e.testEdgeBits (Graph.SUCCESSOR_EDGE) && e.isSource (n))
				{
					m = e.getTarget ();
					break;
				}
			}
			if ((m == null) || condition.evaluateBoolean (m))
			{
				return m;
			}
			n = m;
		}
	}

	/**
	 * This generator method yields all descendants of <code>n</code>
	 * which are instances of <code>cls</code>.
	 * Descendants are those nodes which can be reached by
	 * traversing {@link #successor}- or {@link #branch}-edges forwards.
	 * 
	 * @param cb a consumer instance receiving the nodes (provided by the XL compiler)
	 * @param n a node
	 * @param cls the type to seach for
	 * @return <code>null</code>
	 */
	public static <T> Node descendants (ObjectConsumer<? super T> cb, Node n, Class<T> cls)
	{
		// non-recursive implementation; uses the following stack
		ObjectList<Node> stack = new ObjectList<Node> (100);
		stack.push (n);
		while (!stack.isEmpty ())
		{
			// get next node to handle
			n = stack.pop ();
			if (cls.isInstance (n))
			{
				// yield node to consumer
				cb.consume ((T) n);
			}
			// iterate through all edges of n
			for (Edge e = n.getFirstEdge (); e != null; e = e.getNext (n))
			{
				// get target node of e
				Node t = e.getTarget ();
				// check if edge is traversed in forward direction and if it
				// is a branching or successor edge
				if ((t != n) && e.testEdgeBits (branch | successor))
				{
					// further descendant found, push on top of stack
					stack.push (t);
				}
			}
		}
		return null;
	}	

	/**
	 * Creates a condition which is fulfilled for an object
	 * iff it is an instance of at least one of the specified
	 * <code>classes</code> (or iff it is not an instance
	 * in case <code>isTrue</code> is <code>false</code>). 
	 * 
	 * @param classes list of classes
	 * @param isTrue condition value iff object is an instance of one of <code>classes</code> 
	 * @return a condition
	 */
	public static NodeFilter<Node> filter (final Class[] classes,
			final boolean isTrue)
	{
		return new NodeFilter<Node> ()
		{
			@Override
			public boolean evaluateBoolean (Node node)
			{
				Class[] cls = classes;
				for (int i = cls.length - 1; i >= 0; i--)
				{
					if (cls[i].isInstance (node))
					{
						return isTrue;
					}
				}
				return !isTrue;
			}
		};
	}

	/**
	 * Computes the global coordinate transformation of the <code>node</code>.
	 * The returned matrix must not be modified.
	 * 
	 * @param node a node
	 * @return the node's global coordinate transformation
	 */
	public static Matrix34d transformation (Node node)
	{
		return GlobalTransformation.get (node, true, GraphState.current (node
			.getGraph ()), false);
	}

	/**
	 * Computes the global coordinate transformation of the <code>node</code>.
	 * If <code>post</code> is <code>false</code>, the transformation of
	 * the <code>node</code> itself is returned, otherwise the
	 * transformation of its children. These may differ if <code>node</code>
	 * induces a change of coordinates.
	 * The returned matrix must not be modified.
	 * 
	 * @param node a node
	 * @param post <code>true</code> iff the post-transformation (of the
	 * node's children) shall be returned
	 * @return the node's global coordinate transformation
	 */
	public static Matrix34d transformation (Node node, boolean post)
	{
		return GlobalTransformation.get (node, true, GraphState.current (node
			.getGraph ()), post);
	}

	/**
	 * Computes the location of <code>node</code> in global coordinates.
	 * The computed value is written to <code>location</code>. The location
	 * is defined by the point (0, 0, 0) in local coordinates of the
	 * <code>node</code> if <code>end</code> is <code>false</code>,
	 * or of the <code>node</code>'s children if <code>end</code> is <code>true</code>.
	 * 
	 * @param loc receives the location
	 * @param node a node
	 * @param end use location or end location of <code>node</code>
	 */
	public static void location (Tuple3d loc, Node node, boolean end)
	{
		Matrix34d m = transformation (node, end);
		loc.x = m.m03;
		loc.y = m.m13;
		loc.z = m.m23;
	}

	/**
	 * Computes the growth direction of the turtle
	 * at <code>node</code> in global coordinates.
	 * The computed value is written to <code>direction</code>. The direction
	 * is defined by the direction (0, 0, 1) in local coordinates of the
	 * <code>node</code> if <code>end</code> is <code>false</code>,
	 * or of the <code>node</code>'s children if <code>end</code> is <code>true</code>.
	 * 
	 * @param dir receives the direction
	 * @param node a node
	 * @param end use location or end location of <code>node</code>
	 */
	public static void direction (Tuple3d dir, Node node, boolean end)
	{
		Matrix34d m = transformation (node, end);
		dir.x = m.m02;
		dir.y = m.m12;
		dir.z = m.m22;
	}

	/**
	 * Computes the location of <code>node</code> in global coordinates.
	 * The location is defined by the point (0, 0, 0) in local coordinates.
	 * 
	 * @param node a node
	 * @return the <code>node</code>'s location in global coordinates
	 * 
	 * @see #location(Tuple3d, Node, boolean)
	 */
	public static Point3d location (Node node)
	{
		Point3d p = new Point3d ();
		location (p, node, false);
		return p;
	}

	/**
	 * The same as {@link #location(Node)}. This method is designed
	 * as an auto-conversion method.
	 * 
	 * @param node a node
	 * @return the <code>node</code>'s location in global coordinates
	 */
	public static Point3d toPoint3d (Node node)
	{
		Point3d p = new Point3d ();
		location (p, node, false);
		return p;
	}

	public static Tuple3d toTuple3d (Node node)
	{
		return toPoint3d (node);
	}

	/**
	 * Computes the growth direction of the turtle
	 * at <code>node</code> in global coordinates.
	 * The direction is defined by the direction (0, 0, 1) in local coordinates.
	 * 
	 * @param node a node
	 * @return the turtle's growth direction at <code>node</code>
	 * 
	 * @see #direction(Tuple3d, Node, boolean)
	 */
	public static Vector3d direction (Node node)
	{
		Vector3d v = new Vector3d ();
		direction (v, node, false);
		return v;
	}

	/**
	 * Computes the end location of <code>node</code> in global coordinates.
	 * The end location is defined by the point (0, 0, 0) in local coordinates
	 * of <code>node</code>'s children.
	 * 
	 * @param node a node
	 * @return the <code>node</code>'s end location in global coordinates
	 * 
	 * @see #location(Tuple3d, Node, boolean)
	 */
	public static Point3d endlocation (Node node)
	{
		Point3d p = new Point3d ();
		location (p, node, true);
		return p;
	}

	/**
	 * Computes the growth direction of the turtle
	 * at the end of <code>node</code> in global coordinates.
	 * The direction is defined by the direction (0, 0, 1) in
	 * local coordinates of <code>node</code>'s children.
	 * 
	 * @param node a node
	 * @return the turtle's growth direction at <code>node</code>'s
	 * children
	 * 
	 * @see #direction(Tuple3d, Node, boolean)
	 */
	public static Vector3d enddirection (Node node)
	{
		Vector3d v = new Vector3d ();
		direction (v, node, true);
		return v;
	}

	
	/**
	 * Computes the rotation angle in degrees of the transformation
	 * defined by <code>node</code>. The computed value will be correct
	 * if the transformation is a rotation, possibly combined with a uniform
	 * scaling.
	 * 
	 * @param node a node
	 * @return the rotation angle of the transformation of <code>node</code>,
	 * in degrees
	 */
	public static double angle (Node node)
	{
		GraphState gs = GraphState.current (node.getGraph ());
		Matrix3d m = new Matrix3d ();
		GlobalTransformation.getParentValue (node, true, gs, true).getRotationScale (m);
		m.invert ();
		Math2.lmul (m, GlobalTransformation.get (node, true, gs, true));
		double trace = m.m00 + m.m11 + m.m22;
		double det = m.determinant ();
		if (det < 0)
		{
			trace = -trace;
			det = -det;
		}
		trace *= Math.pow (det, -1d / 3);
		double cos = (trace - 1) * 0.5;
		return (cos >= 1) ? 0 : (cos <= -1) ? 180 : Math.acos (cos) * (180 / Math.PI);
	}

	public static double inclination (Node node)
	{
		Matrix34d m = GlobalTransformation.get (node, true, GraphState.current (node.getGraph ()), true);
		double sin = m.m22 / (m.m02 * m.m02 + m.m12 * m.m12 + m.m22 * m.m22);
		return (sin >= 1) ? 90 : (sin <= -1) ? -90 : Math.asin (sin) * R2D;
	}

	public static double angle (Tuple3d a, Tuple3d b)
	{
		double l2 = Math2.dot (a, a) * Math2.dot (b, b);
		if (l2 <= 0)
		{
			return 0;
		}
		double cos = Math2.dot (a, b) / Math.sqrt (l2);
		return (cos >= 1) ? 0 : (cos <= -1) ? 180 : Math.acos (cos) * R2D;
	}

	public static void setRandomTransform (Null target, Transform3D center,
			double sigma, double minX, double minY, double maxX, double maxY)
	{
		if ((minX >= maxX) || (minY >= maxY))
		{
			throw new IllegalArgumentException ();
		}
		double x = 0;
		double y = 0;
		if (center != null)
		{
			Matrix4d m = new Matrix4d ();
			m.setIdentity ();
			center.transform (m, m);
			x = m.m03;
			y = m.m13;
		}
		Random r = Operators.getRandomGenerator ();
		double phi = r.nextDouble () * (2 * Math.PI);
		sigma *= r.nextGaussian ();
		x += Math.cos (phi) * sigma;
		y += Math.sin (phi) * sigma;
		while (true)
		{
			if (x < minX)
			{
				x = 2 * minX - x;
			}
			else if (x > maxX)
			{
				x = 2 * maxX - x;
			}
			else
			{
				break;
			}
		}
		while (true)
		{
			if (y < minY)
			{
				y = 2 * minY - y;
			}
			else if (y > maxY)
			{
				y = 2 * maxY - y;
			}
			else
			{
				break;
			}
		}
		Null.transform$FIELD.setObject (target, null, new TVector3d (x, y, 0),
			(target.getGraph () != null) ? target.getGraph ()
				.getActiveTransaction () : null);
	}

	/**
	 * Calculate squared distance between node a an node b.
	 * @param a node a
	 * @param b node b
	 * @return squared distance between a and b
	 */
	public static double distanceSquared (Node a, Node b)
	{
		Matrix34d m = transformation (a);
		double x = m.m03, y = m.m13, z = m.m23;
		m = transformation (b);
		double t;
		return ((t = (x - m.m03)) * t + (t = (y - m.m13)) * t + (t = (z - m.m23))
			* t);
	}

	/**
	 * Computes the global distance between two nodes. The distance is
	 * measured between the points (0, 0, 0) in the local coordinate
	 * systems of the nodes.
	 * 
	 * @param a first node
	 * @param b second node
	 * @return global distance between nodes
	 */
	public static double distance (Node a, Node b)
	{
		return Math.sqrt (distanceSquared (a, b));
	}

	/**
	 * Returns a cone whose tip is located at <code>origin</code>,
	 * whose central axis has <code>direction</code> as direction,
	 * and whose half opening angle is <code>angle</code> (in degrees).
	 * The cone has no base plane, i.e., it extends to infinity.
	 * 
	 * @param origin location of the cone's tip
	 * @param direction direction of the cone's axis
	 * @param angle half opening angle in degrees
	 * @return instance of <code>Cone</code> corresponding to the specified geometry
	 */
	public static Cone cone (Tuple3d origin, Vector3d direction, double angle)
	{
		Cone c = new Cone ();
		c.setTransformation (origin, direction);
		angle = 1 / Math.tan (angle * DEG);
		c.scale (angle, angle, 1);
		c.base = Double.POSITIVE_INFINITY;
		return c;
	}

	/**
	 * Returns a cone whose tip is defined by the location
	 * of <code>node</code>. Depending on <code>end</code>, the location
	 * or end location of <code>node</code> is used
	 * (see {@link #location(Tuple3d, Node, boolean)}).
	 * The axis of the cone points into the local z-direction
	 * of the coordinate system of <code>node</code>
	 * or <code>node</code>'s children (again depending on <code>end</code>).
	 * The cone has no base plane, i.e., it extends to infinity.
	 * 
	 * @param node node which defines the cone's tip
	 * @param end use location or end location of <code>node</code>
	 * @param angle half opening angle in degrees
	 * @return instance of <code>Cone</code> corresponding to the specified geometry
	 */
	public static Cone cone (Node node, boolean end, double angle)
	{
		Matrix34d m = transformation (node, end);
		return cone (new Point3d (m.m03, m.m13, m.m23), new Vector3d (m.m02,
			m.m12, m.m22), angle);
	}

	/**
	 * Constructs a <code>Line</code> which represents a ray emanating at
	 * <code>origin</code> in the specified <code>direction</code>. The
	 * <code>start</code> parameter of the line is set to 0, the
	 * <code>end</code> parameter is set to infinity.
	 * 
	 * @param origin origin of the ray
	 * @param direction direction of the ray
	 * @return new ray
	 */
	public static Line ray (Tuple3d origin, Tuple3d direction)
	{
		Line line = new Line ();
		line.origin.set (origin);
		line.direction.set (direction);
		line.start = 0;
		line.end = Double.POSITIVE_INFINITY;
		return line;
	}

	/**
	 * Compute all intersections between <code>line</code> and the surface
	 * of <code>volume</code>. The returned list is valid until the next
	 * invocation of this method within the same thread.
	 * 
	 * @param line a line
	 * @param volume a volume
	 * @return list containing all intersections of the line
	 * and the volume's surface
	 */
	public static IntersectionList intersect (Line line, Volume volume)
	{
		IntersectionList list = currentLocals ().ilist;
		list.setSize (0);
		volume.computeIntersections (line, Intersection.ALL, list, null, null);
		return list;
	}

	/**
	 * This operator method is an alias for {@link #intersect}.
	 * 
	 * @param line a line
	 * @param volume a volume
	 * @return list containing all intersections of the line
	 * and the volume's surface
	 */
	public static IntersectionList operator$and (Line line, Volume volume)
	{
		return intersect (line, volume);
	}

	/**
	 * This autoconversion method returns the first intersection point of
	 * <code>list</code>, or <code>null</code> if <code>list</code> is empty.
	 * The returned point may be modified.
	 * 
	 * @param list list of previously computed intersections
	 * @return first intersection point, or <code>null</code>
	 */
	public static Point3d toPoint3d (IntersectionList list)
	{
		return (list.size > 0) ? new Point3d (list.elements[0].getPoint ())
				: null;
	}

	/**
	 * This method returns the height of a volume at a point <code>(x, y)</code>
	 * (in global coordinates). This is the maximum z-coordinate of the volume
	 * at <code>(x, y)</code>. If the volume does not contain a point with
	 * matching xy-coordinates, 0 is returned.
	 * 
	 * @param volume a volume
	 * @param x global x-coordinate
	 * @param y global y-coordinate
	 * @return height of <code>volume</code> at <code>(x, y)</code>, or 0
	 */
	public static double height (Volume volume, double x, double y)
	{
		Locals loc = currentLocals ();
		Line line = loc.line;
		line.origin.set (x, y, 0);
		line.direction.set (0, 0, -1);
		line.start = -Double.MAX_VALUE;
		line.end = Double.MAX_VALUE;
		IntersectionList list = loc.ilist;
		list.setSize (0);
		volume.computeIntersections (line, Intersection.CLOSEST, list, null,
			null);
		return (list.size > 0) ? list.elements[0].getPoint ().z : 0;
	}

	/**
	 * Returns a line which corresponds to the central line of <code>node</code>.
	 * The central line starts at the {@link #location(Node)} of the node
	 * and extends to the {@link #endlocation(Node)} of the node.
	 * 
	 * @param node a node
	 * @return central line of the node
	 */
	public static Line toLine (Node node)
	{
		GraphState gs = GraphState.current (node.getGraph ());
		Matrix34dPair t = GlobalTransformation.ATTRIBUTE.get (node, true, gs);
		Matrix34d f = t.get (false);
		Matrix34d s = t.get (true);
		Point3d p = new Point3d (f.m03, f.m13, f.m23);
		Vector3d v = new Vector3d (s.m03, s.m13, s.m23);
		v.sub (p);
		return new Line (p, v, 0, 1);
	}

	/**
	 * Returns a volume which corresponds to the shape of
	 * <code>node</code>. If <code>node</code> has no shape, an empty
	 * volume is returned.
	 * 
	 * @param node a node
	 * @return volume corresponding the <code>node</code>'s shape
	 */
	public static Volume volume (Node node)
	{
		return VolumeAttribute.getVolume (node, true, graphState ());
	}

	/**
	 * Returns the distance between <code>point</code> and
	 * <code>line</code>.
	 * 
	 * @param point a point
	 * @param line a line
	 * @return distance between <code>point</code> and <code>line</code>
	 * 
	 * @see Line#distance(Tuple3d)
	 */
	public static double distanceToLine (Tuple3d point, Line line)
	{
		return line.distance (point);
	}

	/**
	 * Determines the fraction of the <code>line</code> which
	 * intersects the specified <code>cone</code>. 
	 * 
	 * @param line line to intersect
	 * @param cone cone to intersect
	 * @return fraction of <code>line</code> which intersects <code>cone</code>
	 */
	public static double intersectionLength (Line line, Cone cone)
	{
		double x = line.end - line.start;
		return cone.intersect (line) ? (line.end - line.start) / x : -0;
	}

	/**
	 * Computes the rotation matrix which implements a directional
	 * tropism towards a <code>direction</code>. This is done as follows:
	 * The matrix <code>m</code> is interpreted as the transformation
	 * matrix from local to global coordinates. Thus, the local z-direction
	 * (the growth direction) has the value
	 * <code>v = (m.m02, m.m12, m.m22)</code>
	 * in global coordinates. Now the cross product
	 * <code>d = v<sup>0</sup> &#215; direction</code>,
	 * where <code>v<sup>0</sup></code> is the unit vector in direction of
	 * <code>v</code>, defines the axis about which
	 * a rotation has to be performed in order to achieve the desired tropism. This
	 * direction is transformed to the local coordinates of <code>m</code>,
	 * and <code>out</code> is set to a rotation about this
	 * transformed direction, its angle being the length
	 * of <code>d</code> multiplied by <code>e</code> (in radians). 
	 * <br>
	 * If the absolute value of the angle is less than <code>1e-10</code>,
	 * no rotation matrix is computed and this method returns
	 * <code>false</code>. Then <code>out</code> does not
	 * contain a valid rotation matrix and should be ignored.
	 * This case happens when the growth direction 
	 * is (anti-)parallel to the desired <code>direction</code>
	 * of the tropism.
	 * 
	 * @param m transformation matrix from local to global coordinates
	 * @param direction direction of the tropism in global coordinates
	 * @param e strength of the tropism
	 * @param out the computed rotation matrix is placed in here (only
	 * valid if the returned value is <code>true</code>)
	 * @return <code>true</code> iff a rotation matrix has been computed
	 */
	public static boolean directionalTropism (Matrix34d m, Tuple3d direction,
			float e, Matrix4d out)
	{
		Vector3d dir = new Vector3d (direction.z * m.m12 - direction.y * m.m22,
			direction.x * m.m22 - direction.z * m.m02, direction.y * m.m02
				- direction.x * m.m12);
		e *= Math.sqrt (dir.lengthSquared ()
			/ (m.m02 * m.m02 + m.m12 * m.m12 + m.m22 * m.m22));
		if (e * e >= 1e-20)
		{
			Math2.invTransformVector (m, dir);
			out.set (new AxisAngle4d (dir, e));
			return true;
		}
		return false;
	}

	/**
	 * Computes the rotation matrix which implements a positional
	 * tropism towards a <code>target</code> location. For the
	 * details of computation, see {@link #directionalTropism},
	 * where the <code>direction</code> argument is the normalized
	 * direction vector from the origin of the local coordinate system
	 * <code>(m.m03, m.m13, m.m23)</code> to <code>target</code>.
	 * <br>
	 * If, for mathematical reasons, no rotation matrix can be computed
	 * or if no rotation is needed because the local growth direction
	 * already points to <code>target</code>, this method returns
	 * <code>false</code>. In this case, <code>out</code> does not
	 * contain a valid rotation matrix and should be ignored.
	 * 
	 * @param m transformation matrix from local to global coordinates
	 * @param target target location of the tropism in global coordinates
	 * @param e strength of the tropism
	 * @param out the computed rotation matrix is placed in here (only
	 * valid if the returned value is <code>true</code>)
	 * @return <code>true</code> iff a rotation matrix has been computed
	 */
	public static boolean positionalTropism (Matrix34d m, Tuple3d target,
			float e, Matrix4d out)
	{
		double x = target.x - m.m03, y = target.y - m.m13, z = target.z - m.m23;
		double l = x * x + y * y + z * z;
		if (l > 0)
		{
			Vector3d dir = new Vector3d (z * m.m12 - y * m.m22, x * m.m22 - z
				* m.m02, y * m.m02 - x * m.m12);
			e *= Math.sqrt (dir.lengthSquared ()
				/ (l * (m.m02 * m.m02 + m.m12 * m.m12 + m.m22 * m.m22)));
			if (e * e >= 1e-20)
			{
				Math2.invTransformVector (m, dir);
				out.set (new AxisAngle4d (dir, e));
				return true;
			}
		}
		return false;
	}

	/**
	 * Computes the rotation matrix which implements an orthogonal
	 * tropism into a plane perpendicular to <code>direction</code>. This is done as follows:
	 * The matrix <code>m</code> is interpreted as the transformation
	 * matrix from local to global coordinates. Thus, the local z-direction
	 * (the growth direction) has the value
	 * <code>v = (m.m02, m.m12, m.m22)</code>
	 * in global coordinates. Now the cross product
	 * <code>d = v<sup>0</sup> &#215; direction</code>,
	 * where <code>v<sup>0</sup></code> is the unit vector in direction of
	 * <code>v</code>, defines the axis about which
	 * a rotation has to be performed in order to achieve the desired tropism. This
	 * direction is transformed to the local coordinates of <code>m</code>,
	 * and <code>out</code> is set to a rotation about this
	 * transformed direction, its angle being the negated value
	 * of the scalar product <code>v<sup>0</sup> &#183; direction</code>
	 * multiplied by <code>e</code> (in radians). 
	 * <br>
	 * If the absolute value of the angle is less than <code>1e-10</code>,
	 * no rotation matrix is computed and this method returns
	 * <code>false</code>. Then <code>out</code> does not
	 * contain a valid rotation matrix and should be ignored.
	 * This case happens when the growth direction 
	 * is (anti-)parallel to the desired <code>direction</code>
	 * of the tropism.
	 * 
	 * @param m transformation matrix from local to global coordinates
	 * @param direction direction of the tropism in global coordinates
	 * @param e strength of the tropism
	 * @param out the computed rotation matrix is placed in here (only
	 * valid if the returned value is <code>true</code>)
	 * @return <code>true</code> iff a rotation matrix has been computed
	 */
	public static boolean orthogonalTropism (Matrix34d m, Tuple3d direction,
			float e, Matrix4d out)
	{
		Vector3d dir = new Vector3d (direction.z * m.m12 - direction.y * m.m22,
			direction.x * m.m22 - direction.z * m.m02, direction.y * m.m02
				- direction.x * m.m12);
		e *= -(m.m02 * direction.x + m.m12 * direction.y + m.m22 * direction.z)
			/ Math.sqrt (m.m02 * m.m02 + m.m12 * m.m12 + m.m22 * m.m22);
		if (e * e >= 1e-20)
		{
			Math2.invTransformVector (m, dir);
			out.set (new AxisAngle4d (dir, e));
			return true;
		}
		return false;
	}

	/**
	 * Return a new node with a transformation matrix set to the desired tropism.
	 * @param a node containing original orientation
	 * @param direction target direction towards the tropism should orient to
	 * @param e strength of the tropism
	 * @return a node with a transformation set to the requested tropism
	 * @see #directionalTropism
	 */
	public static Null tropism (Node a, Vector3d direction, float e)
	{
		TMatrix4d t = new TMatrix4d ();
		return directionalTropism (transformation (a, true), direction, e, t) ? new Null (
			t)
				: new Null ();
	}

	/**
	 * Return a new node with a transformation matrix set to the desired tropism.
	 * @param a node containing original orientation
	 * @param target target location towards the tropism should orient to
	 * @param e strength of the tropism
	 * @return a node with a transformation set to the requested tropism
	 * @see #positionalTropism
	 */
	public static Null tropism (Node a, Point3d target, float e)
	{
		TMatrix4d t = new TMatrix4d ();
		return positionalTropism (transformation (a, true), target, e, t) ? new Null (
			t)
				: new Null ();
	}

	/**
	 * This method clones the subgraph starting at <code>root</code>
	 * and returns the cloned node which corresponds to <code>root</code>.
	 * The subgraph consists of all nodes which can be reached
	 * by traversing edges in forward direction. Nodes are cloned
	 * deeply, i.e., field values are cloned, too.
	 * 
	 * @param root root node of subgraph
	 * @return corresponding root node of cloned subgraph
	 * @throws CloneNotSupportedException
	 */
	public static Node cloneSubgraph (Node root)
		throws CloneNotSupportedException
	{
		return root.cloneGraph (EdgePatternImpl.FORWARD, true);
	}

	public static Node cloneNode (Node node) throws CloneNotSupportedException
	{
		return node.clone (true);
	}

	/*!!
	 #foreach ($type in $primitives)
	 $pp.setType($type)

	 /**
	 * Returns the <code>$type</code> value which is wrapped
	 * in <code>w</code> if <code>w</code> is an instance of
	 * {@link ${pp.Type}Node}. Otherwise this method returns
	 * ${pp.simplenull}.
	 * 
	 * @param w a node
	 * @return wrapped <code>$type</code> value, or ${pp.simplenull}
	 $C
	 public static $type ${pp.type}Value (Node w)
	 {
	 return (w instanceof ${pp.Type}Node) ? ((${pp.Type}Node) w).getValue ()
	 : ${pp.null};
	 }

	 #end
	 !!*/
//!! #* Start of generated code
	 	 
// generated
	 /**
	 * Returns the <code>boolean</code> value which is wrapped
	 * in <code>w</code> if <code>w</code> is an instance of
	 * {@link BooleanNode}. Otherwise this method returns
	 * false.
	 * 
	 * @param w a node
	 * @return wrapped <code>boolean</code> value, or false
	 */
	 public static boolean booleanValue (Node w)
	 {
	 return (w instanceof BooleanNode) ? ((BooleanNode) w).getValue ()
	 : false;
	 }
// generated
	 	 
// generated
	 /**
	 * Returns the <code>byte</code> value which is wrapped
	 * in <code>w</code> if <code>w</code> is an instance of
	 * {@link ByteNode}. Otherwise this method returns
	 * 0.
	 * 
	 * @param w a node
	 * @return wrapped <code>byte</code> value, or 0
	 */
	 public static byte byteValue (Node w)
	 {
	 return (w instanceof ByteNode) ? ((ByteNode) w).getValue ()
	 : ((byte) 0);
	 }
// generated
	 	 
// generated
	 /**
	 * Returns the <code>short</code> value which is wrapped
	 * in <code>w</code> if <code>w</code> is an instance of
	 * {@link ShortNode}. Otherwise this method returns
	 * 0.
	 * 
	 * @param w a node
	 * @return wrapped <code>short</code> value, or 0
	 */
	 public static short shortValue (Node w)
	 {
	 return (w instanceof ShortNode) ? ((ShortNode) w).getValue ()
	 : ((short) 0);
	 }
// generated
	 	 
// generated
	 /**
	 * Returns the <code>char</code> value which is wrapped
	 * in <code>w</code> if <code>w</code> is an instance of
	 * {@link CharNode}. Otherwise this method returns
	 * 0.
	 * 
	 * @param w a node
	 * @return wrapped <code>char</code> value, or 0
	 */
	 public static char charValue (Node w)
	 {
	 return (w instanceof CharNode) ? ((CharNode) w).getValue ()
	 : ((char) 0);
	 }
// generated
	 	 
// generated
	 /**
	 * Returns the <code>int</code> value which is wrapped
	 * in <code>w</code> if <code>w</code> is an instance of
	 * {@link IntNode}. Otherwise this method returns
	 * 0.
	 * 
	 * @param w a node
	 * @return wrapped <code>int</code> value, or 0
	 */
	 public static int intValue (Node w)
	 {
	 return (w instanceof IntNode) ? ((IntNode) w).getValue ()
	 : ((int) 0);
	 }
// generated
	 	 
// generated
	 /**
	 * Returns the <code>long</code> value which is wrapped
	 * in <code>w</code> if <code>w</code> is an instance of
	 * {@link LongNode}. Otherwise this method returns
	 * 0.
	 * 
	 * @param w a node
	 * @return wrapped <code>long</code> value, or 0
	 */
	 public static long longValue (Node w)
	 {
	 return (w instanceof LongNode) ? ((LongNode) w).getValue ()
	 : ((long) 0);
	 }
// generated
	 	 
// generated
	 /**
	 * Returns the <code>float</code> value which is wrapped
	 * in <code>w</code> if <code>w</code> is an instance of
	 * {@link FloatNode}. Otherwise this method returns
	 * 0.
	 * 
	 * @param w a node
	 * @return wrapped <code>float</code> value, or 0
	 */
	 public static float floatValue (Node w)
	 {
	 return (w instanceof FloatNode) ? ((FloatNode) w).getValue ()
	 : ((float) 0);
	 }
// generated
	 	 
// generated
	 /**
	 * Returns the <code>double</code> value which is wrapped
	 * in <code>w</code> if <code>w</code> is an instance of
	 * {@link DoubleNode}. Otherwise this method returns
	 * 0.
	 * 
	 * @param w a node
	 * @return wrapped <code>double</code> value, or 0
	 */
	 public static double doubleValue (Node w)
	 {
	 return (w instanceof DoubleNode) ? ((DoubleNode) w).getValue ()
	 : ((double) 0);
	 }
// generated
//!! *# End of generated code

	/**
	 * Returns the <code>Object</code> value which is wrapped
	 * in <code>w</code> if <code>w</code> is an instance of
	 * {@link ObjectNode}, {@link NURBSCurve} (a
	 * <code>NURBSCurve</code> node wraps its
	 * contained {@link de.grogra.math.BSplineCurve})
	 * or {@link NURBSSurface} (a
	 * <code>NURBSSurface</code> node wraps its
	 * contained {@link de.grogra.math.BSplineSurface}).
	 * Otherwise this method returns null.
	 * 
	 * @param w a node
	 * @return wrapped <code>Object</code> value, or null
	 */
	public static Object objectValue (Node w)
	{
		return (w instanceof ObjectNode) ? ((ObjectNode) w).getValue ()
				: (w instanceof NURBSCurve) ? ((NURBSCurve) w).getCurve ()
				: (w instanceof NURBSSurface) ? ((NURBSSurface) w).getSurface ()
				: null;
	}

	/**
	 * Determines if <code>n</code> has been selected in the
	 * workbench by the user. 
	 * 
	 * @param n a node
	 * @return <code>true</code> iff <code>n</code> is contained
	 * in the current selection
	 */
	public static boolean isSelected (Node n)
	{
		Workbench w = Workbench.current ();
		if (w != null)
		{
			Object s = UIProperty.WORKBENCH_SELECTION.getValue (w);
			return (s instanceof GraphSelection)
				&& ((GraphSelection) s).contains (n.getGraph (), n, true);
		}
		return false;
	}

	/**
	 * Returns a <code>Function</code> instance which refers
	 * to the function named <code>name</code>. The list of functions
	 * in GroIMP is available in the panel
	 * "Object Explorers/Math Objects/Functions".
	 * 
	 * @param name name of function within GroIMP
	 * @return <code>FunctionRef</code> referring to the named function
	 */
	public static FunctionRef function (String name)
	{
		return new FunctionRef (name);
	}

	/**
	 * Returns a <code>CurveRef</code> instance which refers
	 * to the curve named <code>name</code>. The list of curves
	 * in GroIMP is available in the panel
	 * "Object Explorers/Math Objects/Curves".
	 * 
	 * @param name name of curve within GroIMP
	 * @return <code>CurveRef</code> referring to the named curve
	 */
	public static CurveRef curve (String name)
	{
		return new CurveRef (name);
	}

	/**
	 * Returns a <code>SurfaceRef</code> instance which refers
	 * to the surface named <code>name</code>. The list of surfaces
	 * in GroIMP is available in the panel
	 * "Object Explorers/Math Objects/Surfaces".
	 * 
	 * @param name name of surface within GroIMP
	 * @return <code>SurfaceRef</code> referring to the named surface
	 */
	public static SurfaceRef surface (String name)
	{
		return new SurfaceRef (name);
	}

	/**
	 * Returns a <code>DatasetRef</code> instance which refers
	 * to the dataset named <code>name</code>. The list of datasets
	 * in GroIMP is available in the panel
	 * "Object Explorers/Datasets".
	 * 
	 * @param name name of dataset within GroIMP
	 * @return <code>DatasetRef</code> referring to the named dataset
	 */
	public static DatasetRef dataset (String name)
	{
		return new DatasetRef (name);
	}

	/**
	 * Returns a <code>ShaderRef</code> instance which refers
	 * to the shader named <code>name</code>. The list of shaders
	 * in GroIMP is available in the panel
	 * "Object Explorers/3D/Shaders".
	 * 
	 * @param name name of shader within GroIMP
	 * @return <code>ShaderRef</code> referring to the named shader
	 */
	public static ShaderRef shader (String name)
	{
		return new ShaderRef (name);
	}

	// commented out by Uwe Mannl, 2009-10-05
	// used for OpenAlea-GroIMP connection
	// bad implementation, image is loaded at every compilation
//	/**
//	 * Returns a <code>ShaderRef</code> instance which is built
//	 * from an image file.
//	 * 
//	 * @param filename url to the image file
//	 * @return <code>ShaderRef</code> referring to the named shader
//	 */
//	public static ShaderRef createImageShaderFromURL (String filename)
//	{
//		return ShaderRef.shaderFromURL(filename);
//	}

	/**
	 * Returns an <code>ImageRef</code> instance which refers
	 * to the image named <code>name</code>. The list of images
	 * in GroIMP is available in the panel
	 * "Object Explorers/Images".
	 * 
	 * @param name name of image within GroIMP
	 * @return <code>ImageRef</code> referring to the named image
	 */
	public static ImageRef image (String name)
	{
		return new ImageRef (name);
	}

	/**
	 * Returns a <code>SpectrumRef</code> instance which refers
	 * to the spectrum named <code>name</code>. The list of spectra
	 * in GroIMP is available in the panel
	 * "Object Explorers/Light Spectra".
	 * 
	 * @param name name of spectrum within GroIMP
	 * @return <code>ImageRef</code> referring to the named spectrum
	 */
	public static SpectrumRef spectrum (String name)
	{
		return new SpectrumRef (name);
	}

	/**
	 * Returns a <code>LightDistributionRef</code> instance which refers
	 * to the light distribution named <code>name</code>. The list of light distributionRef
	 * in GroIMP is available in the panel
	 * "Object Explorers/3D/lights/Light Distribution".
	 * 
	 * @param name name of light distribution within GroIMP
	 * @return <code>LightDistributionRef</code> referring to the named light distribution
	 */
	public static LightDistributionRef light (String name)
	{
		return new LightDistributionRef (name);
	}

	@Deprecated
	public static MaterialRef material (String name)
	{
		System.err.println ("material(String) is deprecated, use shader(String) instead");
		return new MaterialRef (name);
	}

	/**
	 * Returns a <code>FileRef</code> instance which refers
	 * to the file named <code>name</code>. The list of files
	 * in GroIMP is available in the panel
	 * "File Explorer".
	 * <br>
	 * If <code>name</code> does not contain the character '/'
	 * or the character {@link IO#SYSTEM_ID_SEPARATOR},
	 * <code>name</code> is prefixed by
	 * {@link IO#PROJECT_FS} followed by {@link IO#SYSTEM_ID_SEPARATOR}.
	 * Thus, in this case <code>name</code> is assumed to refer
	 * to a file in the virtual file system of the
	 * project.
	 * 
	 * @param name name of file within GroIMP
	 * @return <code>FileRef</code> referring to the named file
	 */
	public static FileRef file (String name)
	{
		if ((name.indexOf ('/') < 0)
			&& (name.indexOf (IO.SYSTEM_ID_SEPARATOR) < 0))
		{
			name = IO.PROJECT_FS + IO.SYSTEM_ID_SEPARATOR + name;
		}
		return new FileRef (name);
	}

	/**
	 * Returns a <code>Reference</code> instance which refers
	 * to the object named <code>name</code>. The list of objects
	 * in GroIMP is available in the panel
	 * "Object Explorers/Objects".
	 * 
	 * @param name name of object within GroIMP
	 * @return <code>Reference</code> referring to the named object
	 */
	public static Reference reference (String name)
	{
		return new Reference (name);
	}

	/**
	 * Writes the current graph to a file.
	 * 
	 * @param fileName name of the output file
	 */
	public static void exportGraphToFile(final String fileName) {
		IMP.exportGraphToFile(getProjectGraph (), new File(fileName));
	}

	/**
	 * Prints the JavaDoc description of all available commands on the XL console window.
	 * 
	 */
	public static void list() {
		list("");
	}

	/**
	 * Prints a list of all available commands starting with the specified sequence.
	 * 
	 * @param prefix of the commands
	 */
	public static void list(String key) {
		if(key==null) return;
		IMPWorkbench w = workbench ();
		if(w == null) return;
		ArrayList<String> methodNameList = w.getRegistry ().getMethodNameList();
		String oldPrefix = key;
		if(key.length ()==0) {
			println ("\nA", 0xcc0000);
			oldPrefix = "a";
		}
		Iterator<String> iter = methodNameList.iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			if(!name.toLowerCase ().startsWith (oldPrefix) && key.length ()==0) {
				oldPrefix = name.toLowerCase ().substring (0, key.length()+1);
				// print new letter
				println ("\n"+oldPrefix.toUpperCase (), 0xcc0000);
			}
			// print command
			if(name.startsWith (key)) print ("\t"+name, 0x0000cc);
		}
		println("");
	}

	/**
	 * Prints the JavaDoc description of all available commands starting with 
	 * the specified prefix on the XL console window.
	 * 
	 * @param prefix of the commands for those the description should be printed
	 */
	public static void help(String prefix) {
		if(prefix==null || prefix.length ()==0) return;
		IMPWorkbench w = workbench ();
		if(w == null) return;
		ArrayList<MethodDescriptionContent> methodList = w.getRegistry ().getMethodList();
		Iterator<MethodDescriptionContent> iter = methodList.iterator();
		while (iter.hasNext()) {
			MethodDescriptionContent method = iter.next();
			if(method.getName ().toLowerCase ().startsWith (prefix.toLowerCase ())) {
				// print out the method description
				println ("Name:", 0x0000bb);
				println("\t"+method.getType()+" "+method.getName()+"()", 0x333333);
				
				println ("Description:", 0x0000bb);
				println("\t"+method.getDescription().toString ().replace ('[', ' ').replace (']', ' ').replace ('\n', ' ').replace ("  ", " "), 0x333333);

				if(method.getAnnotation().size ()>0) {
					println ("Annotations:", 0x0000bb);
					println("\t"+method.getAnnotation(), 0x333333);
				}
				if(method.getParameter().size ()>0) {
					println ("Parameters:", 0x0000bb);
					println("\t"+method.getParameter(), 0x333333);
				}
				if(method.getAttributeParameter().size ()>0) {
					println ("Attributes:", 0x0000bb);
					println("\t"+method.getAttributeParameter(), 0x333333);
				}
				if(method.getParameter().size ()>0) {
					println ("return comment:", 0x0000bb);
					println("\t"+method.getReturncomment(), 0x333333);
				}
				if(method.getSee().size ()>0) {
					println ("See:", 0x0000bb);
					println("\t"+method.getSee(), 0x333333);
				}
			}
		}
	}

	/**
	 * Creates a rendered image of the actual scene with the 
	 * default camera position and a fixed image size of 800x600.
	 * 
	 * @param fileName name of the output file
	 */
	public static void makeRenderedImage(String fileName) {
		makeRenderedImage(fileName, 800, 600);
	}

	/**
	 * 
	 * Creates a rendered image of the actual scene with the 
	 * default camera position and a variable image size.
	 * 
	 * @param fileName name of the output file
	 * @param width
	 * @param height
	 */
	public static void makeRenderedImage(final String fileName, int width, int height) {
		Raytracer raytracer = new Raytracer(workbench(), width, height);
		try {
			IMP.writeImage(raytracer.computeImage(), new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Creates a rendered image of the actual scene with the 
	 * specified camera and a variable image size.
	 * 
	 * @param fileName name of the output file
	 * @param cameraName name of a specific camera 
	 * @param width
	 * @param height
	 */
	public static void makeRenderedImage(final String fileName, String cameraName, int width, int height) {
		ViewConfig3D v = View3D.getDefaultViewConfig(workbench());		
		v = View3D.withCamera(v,
				(Camera)
				((de.grogra.pf.registry.ObjectItem)
				 workbench().getRegistry()
				 .getItem("/project/objects/3d/cameras/"+cameraName))
				.getObject());
		Raytracer raytracer = new Raytracer(workbench(), v, width, height);	
		try {
			IMP.writeImage(raytracer.computeImage(), new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}	
	
	public static void getCameraPosition(double[] vec)
	{
		ViewConfig3D v = View3D.getDefaultViewConfig(workbench());
		Camera cam = v.getCamera();
		Matrix4d transf = cam.getTransformation();
		transf.getColumn(3,vec);
	}

	/**
	 * Creates a rendered image of the actual scene with the
	 * specified camera and a variable image size.
	 * 
	 * @param fileName name of the output file
	 * @param specific camera 
	 * @param width
	 * @param height
	 */
	public static void makeRenderedImage(final String fileName, Camera camera, int width, int height) {
		ViewConfig3D v = View3D.getDefaultViewConfig(workbench());
		v = View3D.withCamera(v,camera);
		Raytracer raytracer = new Raytracer(workbench(), v, width, height);	
		try {
			IMP.writeImage(raytracer.computeImage(), new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a snapshot of the actual scene.
	 * 
	 * @param fileName name of the output file
	 */
	public static void makeSnapshot(final String fileName) {
		View3D view = View3D.getDefaultView(workbench());
		view.getViewComponent().makeSnapshot(new ObjectConsumer() {
			@Override
			public void consume(Object image) {
				IMP.writeImage((Image)image, new File(fileName));
			}
		});
	}

	/**
	 * Exports the current 3D scene to the specified target file and format.
	 *
	 * Currently supported formats of target files:
	 *  - dxf
	 *  - x3d
	 *  - mtg
	 *  - stl
	 *  - vrml97
	 *  - webgl
	 * 
	 * @param fileName name of the output file
	 * @param format target file format	 
	 */
	public static void export3DScene(String fileName, String format) {
		export3DScene(new File(fileName), format);
	}

	/**
	 * Exports the current 3D scene to the specified target file and format.
	 *
	 * Currently supported formats of target files:
	 *  - dxf
	 *  - x3d
	 *  - mtg
	 *  - stl
	 *  - vrml97
	 *  - webgl
	 * 
	 * @param file output file
	 * @param format target file format	 
	 */
	public static void export3DScene(File file, String format) {
		View view = (View) View3D.getDefaultView(workbench()).getPanel();
		workbench().export(new ObjectSourceImpl(view, "view", view.getFlavor(), 
			view.getWorkbench().getRegistry().getRootRegistry(), null), 
			new MimeType(EXPORT_FORMAT_MAP.get (format.toLowerCase ()), null), file);
	}

	/**
	 * Returns a new, rectangular instance of
	 * <code>Parallelogram</code>
	 * created by the constructor invocation
	 * <code>Parallogram(length, width)</code> and having
	 * {@link RGBAShader#GREEN} as its shader.
	 * 
	 * @param length length of rectangle
	 * @param width width of rectangle
	 * @return green rectangle
	 */
	public static Parallelogram leaf (float length, float width)
	{
		Parallelogram p = new Parallelogram (length, width);
		p.setShader (RGBAShader.GREEN);
		return p;
	}

	/**
	 * Return a <code>MeshNode</code> in shape of a 3d leaf.
	 * 
	 * @return mash of the default 3d leaf
	 */
	public static MeshNode leaf3d () {
		return leaf3d(0);
	}

	/**
	 * Return a <code>MeshNode</code> in shape of it indicated default leaf.
	 * 
	 * @param id of the indicated leaf
	 * @return mash of the default 3d leaf
	 */
	public static MeshNode leaf3d (int id) {
		if(id<0 || id>DEFAULT_LEAF3D.length-1) return triangulate(DEFAULT_LEAF3D[0]);
		return triangulate(DEFAULT_LEAF3D[id]);
	}

	/**
	 * Return a <code>MeshNode</code> in shape of it indicated list of points.
	 * The list of points will be automatically triangulated and returned as <code>MeshNode</code>.
	 * pointlist={x1,y1,z1, ..., xn,yn,zn).
	 * 
	 * @param pointlist 
	 * @return mash of the pointlist
	 */
	public static MeshNode leaf3d (float[] pointlist) {
		return triangulate(pointlist);
	}

	/**
	 * Return a <code>MeshNode</code> in shape of it indicated list of points.
	 * The list of points will be automatically triangulated and returned as <code>MeshNode</code>.
	 * pointlist={x1,y1,z1, ..., xn,yn,zn). First point p0={x1,y1,z1}, 
	 * faces between {{p0,p1,p2}, {p0,p2,p3}, {p0,p3,p4}, ...} 
	 * 
	 * @param pointlist 
	 * @return mash of the pointlist
	 */
	public static MeshNode triangulate (float[] pointlist) {
		// check input data
		if(pointlist.length<9) {
			System.out.println("Error in triangulate: Number of points must be at least three. I switched to the default leaf.");
			pointlist = DEFAULT_LEAF3D[0];
		}
		if(pointlist.length%3!=0) {
			System.out.println("Error in triangulate: Number of points must be a multiple of three. I switched to the default leaf.");
			pointlist = DEFAULT_LEAF3D[0];
		}
		// triangulate data
		FloatList vertexData = new FloatList();
		for(int i=1; i<=pointlist.length/3-2; i++) {
			vertexData.add(pointlist[0]);   vertexData.add(pointlist[1]);   vertexData.add(pointlist[2]);
			vertexData.add(pointlist[i*3+0]); vertexData.add(pointlist[i*3+1]); vertexData.add(pointlist[i*3+2]);
			vertexData.add(pointlist[i*3+3]); vertexData.add(pointlist[i*3+4]); vertexData.add(pointlist[i*3+5]);
		}
		return getMesh(vertexData);
	}

	/**
	 * Calculates the area of the surface generated by the specified list of points.
	 * The list of points will be automatically triangulated.
	 * pointlist={x1,y1,z1, ..., xn,yn,zn). First point p0={x1,y1,z1}, 
	 * faces between {{p0,p1,p2}, {p0,p2,p3}, {p0,p3,p4}, ...}
	 *
	 *Note: Calculation will be wrong, when the described shape is too concave. the resulting area 
	 *will be too large in this case. 
	 *
	 * @param pointlist
	 * @return area 
	 */
	public static double getAreaOfNonTriangulation (float[] pointlist) {
		// check input data
		if(pointlist.length<9) {
			System.out.println("Error in getAreaOfNonTriangulation: Number of points must be at least three. I switched to the default leaf.");
			return 0;
		}
		if(pointlist.length%3!=0) {
			System.out.println("Error in getAreaOfNonTriangulation: Number of points must be a multiple of three. I switched to the default leaf.");
			return 0;
		}

		double area = 0;
		// triangulate data
		for(int i=1; i<=pointlist.length/3-2; i++) {
			double a = Math.sqrt(
					Math.pow((pointlist[0]-pointlist[i*3+0]), 2) + 
					Math.pow((pointlist[1]-pointlist[i*3+1]), 2) + 
					Math.pow((pointlist[2]-pointlist[i*3+2]), 2));
			double b = Math.sqrt(
					Math.pow((pointlist[i*3+0]-pointlist[i*3+3]), 2) + 
					Math.pow((pointlist[i*3+1]-pointlist[i*3+4]), 2) + 
					Math.pow((pointlist[i*3+2]-pointlist[i*3+5]), 2));
			double c = Math.sqrt(
					Math.pow((pointlist[0]-pointlist[i*3+3]), 2) + 
					Math.pow((pointlist[1]-pointlist[i*3+4]), 2) + 
					Math.pow((pointlist[2]-pointlist[i*3+5]), 2));
			//heron's formula
			double s = (a + b + c)/2f;
			area += Math.sqrt(s*(s - a)*(s - b)*(s - c));
		}
		return area;
	}

	/**
	 * Calculates the area of the surface generated by the specified list of points.
	 * It is assumed, that the list of points is triangulated.
	 * pointlist={x1,y1,z1, ..., xn,yn,zn). First point p0={x1,y1,z1}, 
	 * faces between {{p0,p1,p2}, {p3,p4,p5}, {p6,p7,p8}, ...}
	 *
	 * @param pointlist
	 * @return area 
	 */
	public static double getAreaOfTriangulation (float[] pointlist) {
		// check input data
		if(pointlist.length<9) {
			System.out.println("Error in getAreaOfTriangulation: Number of points must be at least three. I switched to the default leaf.");
			return 0;
		}
		if(pointlist.length%3!=0) {
			System.out.println("Error in getAreaOfTriangulation: Number of points must be a multiple of three. I switched to the default leaf.");
			return 0;
		}

		double area = 0;
		// triangulate data
		for(int i=0; i<pointlist.length/9; i++) {
			double a = Math.sqrt(
				Math.pow((pointlist[i*9+0]-pointlist[i*9+6]), 2) + 
				Math.pow((pointlist[i*9+1]-pointlist[i*9+7]), 2) + 
				Math.pow((pointlist[i*9+2]-pointlist[i*9+8]), 2));
			double b = Math.sqrt(
				Math.pow((pointlist[i*9+6]-pointlist[i*9+3]), 2) + 
				Math.pow((pointlist[i*9+7]-pointlist[i*9+4]), 2) + 
				Math.pow((pointlist[i*9+8]-pointlist[i*9+5]), 2));
			double c = Math.sqrt(
				Math.pow((pointlist[i*9+0]-pointlist[i*9+3]), 2) + 
				Math.pow((pointlist[i*9+1]-pointlist[i*9+4]), 2) + 
				Math.pow((pointlist[i*9+2]-pointlist[i*9+5]), 2));
			//heron's formula
			double s = (a + b + c)/2f;
			area += Math.sqrt(s*(s - a)*(s - b)*(s - c));
		}
		return area;
		
		//alternative way: using cross product
		/*
		double sum = 0, a, a1,a2,a3,b1,b2,b3;
		for(int i = 0; i<(pointlist.length/9); i++) {
			
			a1 = pointlist[i*9+6]-pointlist[i*9+3];
			a2 = pointlist[i*9+7]-pointlist[i*9+4];
			a3 = pointlist[i*9+8]-pointlist[i*9+5];
			b1 = pointlist[i*9+6]-pointlist[i*9+0];
			b2 = pointlist[i*9+7]-pointlist[i*9+1];
			b3 = pointlist[i*9+8]-pointlist[i*9+2];

			sum += 0.5 * Math.sqrt( Math.pow(a2*b3-a3*b2,2) + Math.pow(a3*b1-a1*b3,2) + Math.pow(a1*b2-a2*b1,2));;
		}
		return sum;
		*/
	}

	/**
	 * Calculates the area of the surface generated by the specified list of points.
	 * It is assumed, that the list of points is triangulated.
	 * pointlist={x1,y1,z1, ..., xn,yn,zn). First point p0={x1,y1,z1}, 
	 * faces between {{p0,p1,p2}, {p3,p4,p5}, {p6,p7,p8}, ...}
	 * Before the area calculation takes please a scaling is performed. 
	 *
	 * @param scaling factor X
	 * @param scaling factor Y
	 * @param scaling factor Z
	 * @param pointlist
	 * @return area 
	 */
	public static double getAreaOfTriangulation (float scaleX, float scaleY, float scaleZ, float[] pointlist) {
		// check input data
		if(pointlist.length<9) {
			System.out.println("Error in getAreaOfTriangulation: Number of points must be at least three. I switched to the default leaf.");
			return 0;
		}
		if(pointlist.length%3!=0) {
			System.out.println("Error in getAreaOfTriangulation: Number of points must be a multiple of three. I switched to the default leaf.");
			return 0;
		}
		float[] pointlistScaled = new float[pointlist.length];
		for(int i=0; i<pointlist.length/3; i++) {
			//scaling
			pointlistScaled[i*3+0] = scaleX * pointlist[i*3+0];
			pointlistScaled[i*3+1] = scaleY * pointlist[i*3+1];
			pointlistScaled[i*3+2] = scaleZ * pointlist[i*3+2];
		}
		return getAreaOfTriangulation(pointlistScaled);
	}
	
	
	/**
	 * Return a <code>MeshNode</code> in shape of it indicated list of points.
	 * The list of points has to be triangulated.
	 * 
	 * @param vertexData
	 * @return mash of the vertexData
	 */
	public static MeshNode getMesh (FloatList vertexData) {
		//construct a data type, which can handle a list of triangulated mash
		PolygonMesh polygonMesh = new PolygonMesh();
		int[] tmp = new int[vertexData.size()/3];
		for(int i = 0; i<tmp.length; i++) tmp[i]=i;
		// set a list of the indices of the used list of vertices
		// normally = {0,1,2,3,...,n}, where n is the number of used vertices minus one 
		polygonMesh.setIndexData(new IntList(tmp));
		// set the list of vertices
		polygonMesh.setVertexData(vertexData);

		// put the data type (our polygon mesh) into a drawable object
		MeshNode mesh = new MeshNode();
		mesh.setPolygons(polygonMesh);
		mesh.setShader(RGBAShader.GREEN);
		return mesh;
	}

	/**
	 * Return a <code>MeshNode</code> in shape of it indicated list of points.
	 * The list of points will be automatically triangulated and returned as <code>MeshNode</code>.
	 * pointlist={x1,y1,z1, ..., xn,yn,zn). First point p0={x1,y1,z1}, 
	 * faces between {{p0,p1,p2}, {p0,p2,p3}, {p0,p3,p4}, ...} 
	 * 
	 * @param pointlist 
	 * @return mash of the pointlist
	 */
	public static MeshNode triangulate (Point3d[] pointlist) {
		return triangulate(toArray(pointlist));
	}

	/**
	 * Calculates the area of the surface generated by the specified list of points.
	 * The list of points will be automatically triangulated.
	 * pointlist={x1,y1,z1, ..., xn,yn,zn). First point p0={x1,y1,z1}, 
	 * faces between {{p0,p1,p2}, {p0,p2,p3}, {p0,p3,p4}, ...}
	 *
	 *
	 * @param pointlist
	 * @return area 
	 */
	public static double getAreaOfNonTriangulation (Point3d[] pointlist) {
		return getAreaOfNonTriangulation(toArray(pointlist));
	}

	/**
	 * Calculates the area of the surface generated by the specified list of points.
	 * It is assumed, that the list of points is triangulated.
	 * pointlist={x1,y1,z1, ..., xn,yn,zn). First point p0={x1,y1,z1}, 
	 * faces between {{p0,p1,p2}, {p3,p4,p5}, {p6,p7,p8}, ...}
	 *
	 * @param pointlist
	 * @return area 
	 */
	public static double getAreaOfTriangulation (Point3d[] pointlist) {
		return getAreaOfTriangulation(toArray(pointlist));
	}

	/**
	 * Return a <code>MeshNode</code> in shape of it indicated list of points.
	 * The list of points has to be triangulated.
	 * 
	 * @param vertexData
	 * @return mash of the vertexData
	 */
	public static MeshNode getMesh (Point3d[] pointlist) {
		return getMesh(toArray(pointlist));
	}

	private static float[] toArray(Point3d[] pointlist) {
		float[] tmp = new float[pointlist.length*3];
		int i = 0;
		for(Point3d p : pointlist) {
			tmp[i++] = (float)p.x;
			tmp[i++] = (float)p.y;
			tmp[i++] = (float)p.z;
		}
		return tmp;
	}
	
	/**
	 * Returns a <code>MeshNode</code> as visualization of the given convex hull.
	 * 
	 * @param hull
	 * @return mash of the vertexData
	 */
	public static MeshNode getMesh(QuickHull3D hull) {
		int[][] faceIndices = hull.getFaces();
		Point3d[] vertices = hull.getVertices();
		float[] data = new float[faceIndices.length*3*3];
		int j = 0;
		for (int i = 0; i < faceIndices.length; i++) { 
			for (int k = 0; k < faceIndices[i].length; k++) {
				Point3d p = vertices[faceIndices[i][k]];
				data[j++] = (float)p.x;
				data[j++] = (float)p.y;
				data[j++] = (float)p.z;
			}
		}
		return getMesh(data);
	}

	/**
	 * Return a <code>MeshNode</code> in shape of it indicated list of points.
	 * The list of points has to be triangulated.
	 * 
	 * @param pointlist
	 * @return mash of the vertexData
	 */
	public static MeshNode getMesh (float[] pointlist) {
		FloatList vertexData = new FloatList();
		for(int i=0; i<pointlist.length; i++) {
			vertexData.add(pointlist[i]);
		}
		return getMesh(vertexData);
	}

	/**
	 * Return a <code>MeshNode</code> in shape of it indicated list of points.
	 * The list of points has to be triangulated.
	 * The values of the input list will be casted to float.
	 * 
	 * @param pointlist
	 * @return mash of the vertexData
	 */
	public static MeshNode getMesh (double[] pointlist) {
		FloatList vertexData = new FloatList();
		for(int i=0; i<pointlist.length; i++) {
			vertexData.add((float)pointlist[i]);
		}
		return getMesh(vertexData);
	}
	
	/**
	 * Calculates the area of all nodes of the subgraph with the node root as root node
	 * Intersection with other object are not considered.The total area will be calculated.
	 * Keep in mind, that the getSurfaceArea function is only implemented for basic primitive object.
	 * For all other objects it will return zero.
	 * 
	 * @return volume
	 */
	public static double getSurfaceArea(Node root) {
		HashMap<Node, String> visited = new HashMap<Node, String> ();
		ObjectList<Node> toVisit = new ObjectList<Node> ();
		getListOfNodes (root, null, false, visited, toVisit);
		double v = 0;
		for(Node item:visited.keySet()) {
			if(item instanceof ShadedNull) {
				v += ((ShadedNull)item).getSurfaceArea(); 
			}
		}
		return v;
	}

	/**
	 * Calculates the volume of all nodes of the subgraph with the node root as root node.
	 * Intersection with other object are not considered.The total volume will be calculated.
	 * Keep in mind, that the getVolume function is only implemented for basic primitive object.
	 * For all other objects it will return zero.
	 * 
	 * @return volume
	 */
	public static double getVolume(Node root) {
		HashMap<Node, String> visited = new HashMap<Node, String> ();
		ObjectList<Node> toVisit = new ObjectList<Node> ();
		getListOfNodes (root, null, false, visited, toVisit);
		double v = 0;
		for(Node item:visited.keySet()) {
			if(item instanceof ShadedNull) {
				v += ((ShadedNull)item).getVolume(); 
			}
		}
		return v;
	}

	/**
	 * Repaints the View3D panel.
	 * 
	 * When repaint of View3D is deactivated it will be reactivated, repainted and disabled again.
	 * 
	 * Makes use of Dummy nodes (and/or artificial changes of the graph to trigger an repaint) unnecessary.
	 */
	public static void repaintView3D() {
		boolean b = false;
		if(!View.isRepaint()) {
			b = true;
			enableView3DRepaint();
		}
		View3D.getDefaultView(workbench()).repaint();
		if(b) {
			disableView3DRepaint();
		}
	}

	/**
	 * Disables repaint of View3D panel.
	 */
	public static void disableView3DRepaint() {
		View.disableRepaint();
	}

	/**
	 * Enables repaint of View3D panel after it was deactivated with <code>disableView3DRepaint()</code>.
	 */
	public static void enableView3DRepaint() {
		View.enableRepaint();
	}

	/**
	 * Constructs the convex hull of a set of points.
	 * 
	 * @param points input points
	 */
	public static QuickHull3D convexhull(Point3d[] points) {
		QuickHull3D hull = new QuickHull3D();
		hull.setDebug(false);
		hull.build(points);
		hull.triangulate();
		return hull;
	}

	/**
	 * Constructs the convex hull of a set of coordinates.
	 * Assuming 3 values for each point: {x0,y0,z0, x1,y1,z1, ...}
	 * 
	 * @param points input points
	 */
	public static QuickHull3D convexhull(double[] coords) {
		return convexhull(coords, false);
	}

	/**
	 * Constructs the convex hull of a set of coordinates.
	 * Assuming 3 values for each point: {x0,y0,z0, x1,y1,z1, ...}
	 * 
	 * @param points input points
	 * @param debug flag
	 */
	public static QuickHull3D convexhull(double[] coords, boolean debug) {
		QuickHull3D hull = new QuickHull3D();
		hull.setDebug(false);

		hull.build(coords, coords.length/3);
		hull.triangulate();
		return hull;
	}

	/**
	 * Constructs the convex hull of all points that can be reached starting from 
	 * the indicated root node (including the root node).
	 * Only "visible" nodes (nodes instance of ShadedNull) will be take into account.
	 * The location (<code>Library.location(Node)</code>) of each node will be taken as position.
	 * 
	 * @param points input points
	 */
	public static QuickHull3D convexhull(Node root) {
		return convexhull(root, true);
	}

	/**
	 * Constructs the convex hull of all points that can be reached starting from 
	 * the indicated root node.
	 * Only "visible" nodes (nodes instance of ShadedNull) will be take into account.
	 * The location (<code>Library.location(Node)</code>) of each node will be taken as position.
	 * 
	 * @param points input points
	 * @param includeroot input points
	 */
	public static QuickHull3D convexhull(Node root, boolean includeroot) {
		HashMap<Node, String> visited = new HashMap<Node, String> ();
		ObjectList<Node> toVisit = new ObjectList<Node> ();
		getListOfNodes (root, null, false, visited, toVisit);
		int i = 0;
		if(includeroot) visited.put(root,"");
		for(Node item:visited.keySet()) if(item instanceof ShadedNull) i++;
		Point3d[] points = new Point3d[i];
		i = 0;
		for(Node item:visited.keySet()) {
			if(item instanceof ShadedNull) {
				points[i] = new Point3d(location(item));
				i++;
			}
		}
		return convexhull(points);
	}

	public static QuickHull3D convexhull(Node[] nodes) {
		int i = 0;
		Point3d[] points = new Point3d[nodes.length];
		for(Node item:nodes) {
			points[i] = new Point3d(location(item));
			i++;
		}
		return convexhull(points);
	}

	public static Box boundingBox(Node root, boolean includeroot) {
		HashMap<Node, String> visited = new HashMap<Node, String> ();
		ObjectList<Node> toVisit = new ObjectList<Node> ();
		getListOfNodes (root, null, false, visited, toVisit);
		int i = 0;
		if(includeroot) visited.put(root,"");
		for(Node item:visited.keySet()) if(item instanceof ShadedNull) i++;
		Point3d[] points = new Point3d[i];
		i = 0;
		for(Node item:visited.keySet()) {
			if(item instanceof ShadedNull) {
				points[i] = new Point3d(location(item));
				i++;
			}
		}

		double minX=150, minY=150, minZ=150, maxX=-150, maxY=-150, maxZ=-150;
		for(Point3d point : points) {
			if(point.x<minX) minX = point.x;
			if(point.x>maxX) maxX = point.x;
			if(point.y<minY) minY = point.y;
			if(point.y>maxY) maxY = point.y;
			if(point.z<minZ) minZ = point.z;
			if(point.z>maxZ) maxZ = point.z;
		}
		float cX = (float)(Math.abs(maxX-minX));
		float cY = (float)(Math.abs(maxY-minY));
		float cZ = (float)(Math.abs(maxZ-minZ));

		return new Box(cZ/2f, cX, cY/2f);
	}


	/**
	 * Marks all vertices with a Sphere of the given <code>QuickHull3D</code>.
	 * 
	 * @param hull
	 */
	public static void markVertices(QuickHull3D hull) {
		markVertices(hull, 0.04f);
	}

	/**
	 * Marks all vertices with a Sphere of the specified <code>radius</code> and 
	 * of the given <code>QuickHull3D</code>.
	 * 
	 * @param hull
	 * @param radius
	 */
	public static void markVertices(QuickHull3D hull, float radius) {
		markPoints(hull.getVertices(), radius, RGBAShader.BLUE);
	}

	/**
	 * Marks all points with a Sphere of the given <code>QuickHull3D</code>.
	 * 
	 * @param hull
	 */
	public static void markPoints(QuickHull3D hull) {
		markPoints(hull, 0.04f);
	}

	/**
	 * Marks all points with a Sphere of the specified <code>radius</code> and 
	 * of the given <code>QuickHull3D</code>.
	 * 
	 * @param hull
	 * @param radius
	 */
	public static void markPoints(QuickHull3D hull, float radius) {
		markPoints(hull.getPointBuffer(), radius, RGBAShader.RED);
	}

	/**
	 * Marks all points of the specified array with a Sphere of the specified <code>radius</code> and shader.
	 * 
	 * @param hull
	 * @param radius
	 * @param shader
	 */
	public static void markPoints(Point3d[] points, float radius, RGBAShader shader) {
		markPoints(points, radius, shader, false);
	}

	/**
	 * Marks all points of the specified array with a Sphere of the specified <code>radius</code> and shader.
	 * Additionally a TextLabel with a consecutive number can be applyed.
	 * 
	 * @param hull
	 * @param radius
	 * @param shader
	 * @param label
	 */
	public static void markPoints(Point3d[] points, float radius, RGBAShader shader, boolean label) {
		GraphManager g = workbench().getRegistry ().getProjectGraph ();
		Null start = new Null(0,0,0);
		RGGRoot.getRoot (g).addEdgeBitsTo(start, Graph.BRANCH_EDGE, null); //g.getTransaction(false)
		for (int i = 0; i < points.length; i++) {
			Point3d pnt = points[i];
			Sphere marker = new Sphere(radius);
			marker.setTransform(pnt.x, pnt.y, pnt.z);
			marker.setShader(shader);
			if(label) {
				TextLabel tl = new TextLabel(""+i);
				marker.addEdgeBitsTo(tl, Graph.SUCCESSOR_EDGE, null);
			}
			start.addEdgeBitsTo(marker, Graph.BRANCH_EDGE, null);
		}
	}

	/**
	 * Constructs the selected XY-projection of all points that can be reached starting from 
	 * the indicated root node.
	 * Only "visible" nodes (nodes instance of ShadedNull) will be take into account.
	 * The location (<code>Library.location(Node)</code>) of each node will be taken as position.
	 * 
	 * Currently implemented methods:
	 *	CONVEXHULL .. the convex hull
	 *	BOUNDING_RECTANGLE .. bounding rectangle
	 *	ALPHA_SHAPE .. alpha shape with alpha of <code>alpha<code> (Note: In case the alpha shape is not connected, only the first polygon will be returned)
	 *
	 * @param root node
	 * @param method of projection
	 * @param alpha value, only used for alpha shapes
	 */
	public static Point3d[] getXYProjection(Node root, int type, double alpha) {
		HashMap<Node, String> visited = new HashMap<Node, String> ();
		ObjectList<Node> toVisit = new ObjectList<Node> ();
		getListOfNodes (root, null, false, visited, toVisit);
		int i = 0;
		for(Node item:visited.keySet()) if(item instanceof ShadedNull) i++;
		Point2d[] points = new Point2d[i];
		i = 0;
		for(Node item:visited.keySet()) {
			if(item instanceof ShadedNull) {
				Point3d p = location(item);
				points[i] = new Point2d(p.x,p.y);
				i++;
			}
		}
		return getXYProjection(points, type, alpha);
	}

	/**
	 * Constructs the selected XY-projection of all points in the list.
	 * Currently implemented methods:
	 *	CONVEXHULL .. the convex hull
	 *	BOUNDING_RECTANGLE .. bounding rectangle
	 *	ALPHA_SHAPE .. alpha shape with alpha of <code>alpha<code> (Note: In case the alpha shape is not connected, only the first polygon will be returned)
	 *
	 * Returns an array of points: [centroid, p0, p1, p2, ..., pn, p0};
	 * (Repeatation of the first point to close the area (during triangulation))
	 *
	 * @param input points
	 * @param method of projection
	 * @param alpha value, only used for alpha shapes
	 */
	public static Point3d[] getXYProjection(Point3d[] pointlist, int type, double alpha) {
		Point2d[] points = new Point2d[pointlist.length];
		for(int i = 0;i<pointlist.length; i++) {
			points[i] = new Point2d(pointlist[i].x,pointlist[i].y);
		}
		return getXYProjection(points, type, alpha);
	}

	private static Point3d[] getXYProjection(Point2d[] pointlist, int type, double alpha) {
		switch(type) {
			case BOUNDING_RECTANGLE: return getBoundingRectangle(pointlist);
			case CONVEXHULL: return getConvexHull2D(pointlist);
			case ALPHA_SHAPE: return getFirstAlphaShape(pointlist, alpha);
			default: return getBoundingRectangle(pointlist);
		}
	}

	/**
	 * Calculates the bounding rectangle of the given array of points.
	 * Returns an array of points: [centroid, (maxX,maxY,0), (minX,maxY,0), (minX,minY,0), (maxX,minY,0),  (maxX,maxY,0)};
	 * (Repeatation of the first point to close the area (during triangulation))
	 * 
	 * @param points
	 * @return array of points
	 */
	public static Point3d[] getBoundingRectangle(Point2d[] points) {
		double minX=50, minY=50, maxX=-50, maxY=-50;
		for(Point2d point : points) {
			if(point.x<minX) minX = point.x;
			if(point.x>maxX) maxX = point.x;
			if(point.y<minY) minY = point.y;
			if(point.y>maxY) maxY = point.y;
		}
		double cX = minX + Math.abs(maxX-minX)/2d;
		double cY = minY + Math.abs(maxY-minY)/2d;

		return new Point3d[] {
				//centroid
				new Point3d(cX,cY,0),
				//the four corners of the rect
				new Point3d(maxX,maxY,0),new Point3d(minX,maxY,0),
				new Point3d(minX,minY,0),new Point3d(maxX,minY,0),
				//repeatation of the first point to close the area (during triangulation)
				new Point3d(maxX,maxY,0)};
	}

	/**
	 * Calculates the convex hull of the given array of points.
	 * Returns an array of points: [centroid, p0, p1, p2, ..., pn, p0};
	 * (Repeatation of the first point to close the area (during triangulation))
	 * 
	 * @param points
	 * @return array of points
	 */
	public static Point3d[] getConvexHull2D(Point2d[] points) { 
		Point2d[] tmpres = JarvisConvexHull2D.convexHull(points);
		
		Point3d[] res = new Point3d[tmpres.length+2];
		Point2d p0 = JarvisConvexHull2D.getCentroid(tmpres);
		res[0] = new Point3d(p0.x,p0.y,0);
		int i = 1;
		for(Point2d p : tmpres) {
			res[i] = new Point3d(p.x, p.y, 0);
			i++;
		}
		//repeatation the first point to close the area (during triangulation)
		res[tmpres.length+1] = new Point3d(res[1].x,res[1].y,0);
		return res;
	}

	private static Point3d[] getFirstAlphaShape(Point2d[] points, double alpha) {
		List<Vector> data = new ArrayList<Vector>();
		for(Point2d point : points) {
			if(data.size()==0) data.add(new Vector(point.x,point.y));
			//only include this point, if it is not yet present
			if(!data.contains(new Vector(point.x,point.y))) data.add(new Vector(point.x,point.y));
		}
		List<java.util.Vector<Point3d>> res = alpha(data, alpha);
		if(res.size()==0) return new Point3d[] {new Point3d(0,0,0)};
		java.util.Vector<Point3d> v = res.get(0);
		if(v.size()<3) return new Point3d[] {new Point3d(0,0,0)};
		
		Point2d[] tmpres = new Point2d[v.size()];
		for(int i = 0; i<v.size(); i++) tmpres[i] = new Point2d(v.get(i).x,v.get(i).y);
		Point2d p0 = JarvisConvexHull2D.getCentroid(tmpres);
		Point3d[] res2 = new Point3d[tmpres.length+2];
		res2[0] = new Point3d(p0.x,p0.y,0);
		int i = 1;
		for(Point3d p : v) {
			res2[i] = p;
			i++;
		}
		//repeatation the first point to close the area (during triangulation)
		res2[tmpres.length+1] = v.get(0);
		return res2;
	}

	/**
	 * Calculates the alhpa shape of the given array of points considerring the choosn alpah value.
	 * Returns a List of Vectors of Point3d. Each Vector contains the points of one polygon. 
	 * In case the resoulting surface is not coherent the List will contain more then one Vector.
	 * 
	 * @param points
	 * @param alpha value
	 * @return List of Vectors of Point3d, one Vector for each polegon of the surface
	 */
	public static List<java.util.Vector<Point3d>> getAlphaShape(Point2d[] points, double alpha) {
		List<Vector> data = new ArrayList<Vector>();
		for(Point2d point : points) {
			if(data.size()==0) data.add(new Vector(point.x,point.y));
			//only include this point, if it is not yet present
			if(!data.contains(new Vector(point.x,point.y))) data.add(new Vector(point.x,point.y));
		}
		return alpha(data, alpha);
	}

	/**
	 * Calculates the alhpa shape of all (visible) nodes that can be reached from the given 
	 * root Node considerring the choosn alpah value.
	 * Returns a List of Vectors of Point3d. Each Vector contains the points of one polygon. 
	 * In case the resoulting surface is not coherent the List will contain more then one Vector.
	 * 
	 * @param points
	 * @param alpha value
	 * @return List of Vectors of Point3d, one Vector for each polegon of the surface
	 */
	public static List<java.util.Vector<Point3d>> getAlphaShape(Node root, double alpha) {
		HashMap<Node, String> visited = new HashMap<Node, String> ();
		ObjectList<Node> toVisit = new ObjectList<Node> ();
		getListOfNodes (root, null, false, visited, toVisit);
		List<Vector> data = new ArrayList<Vector>();
		for(Node item:visited.keySet()) {
			if(item instanceof ShadedNull) {
				Point3d p = location(item);
				if(data.size()==0) data.add(new Vector(10*p.x,10*p.y));
				//only include this point, if it is not yet present
				if(!data.contains(new Vector(p.x,p.y))) data.add(new Vector(p.x,p.y));
			}
		}
		return alpha(data, alpha);
	}

	private static List<java.util.Vector<Point3d>> alpha(List<Vector> data, double alpha) {	
		AlphaShape as = new AlphaShape(data, alpha);
		List<Polygon> res = as.compute();
		List<java.util.Vector<Point3d>> res2 = new ArrayList<java.util.Vector<Point3d>>();
		if(res.size()!=0) {
			//for each polygon
			for(Polygon p:res) {
				java.util.Vector<Point3d> vertices = new java.util.Vector<Point3d>();
				for (int i = 0; i < p.size(); i++) {
					vertices.add(new Point3d(p.get(i).get(0), p.get(i).get(1), 0));
				}
				res2.add(vertices);
			}
		}
		return res2;
	}

	@Deprecated
	public static NURBSSurface Surface (BSplineSurface surface)
	{
		return new NURBSSurface (surface);
	}


	@Deprecated
	public static NURBSSurface Surface (BSplineCurve profile)
	{
		return new NURBSSurface (profile);
	}


	@Deprecated
	public static NURBSSurface Surface (float radius)
	{
		return new NURBSSurface (radius);
	}


	@Deprecated
	public static NURBSSurface Surface (byte type)
	{
		return new NURBSSurface (type);
	}


	@Deprecated
	public static NURBSSurface Surface (BSplineCurveList profiles)
	{
		return new NURBSSurface (profiles);
	}


	@Deprecated
	public static NURBSSurface Surface (BSplineCurve profile, String name, boolean useRail)
	{
		return new NURBSSurface (profile, name, useRail);
	}


	@Deprecated
	public static NURBSSurface Surface (byte type, String name, boolean useRail)
	{
		return new NURBSSurface (type, name, useRail);
	}


	/**
	 * Returns a color shader whose color represents the value of
	 * <code>x</code> which has to lie between -1 and 1. The color
	 * is interpolated between green for 1, white for 0 and
	 * red for -1.  
	 * 
	 * @param x a value between -1 and 1
	 * @return a color shader which visualizes <code>x</code>
	 */
	public static RGBAShader visualizeAsColor (double x)
	{
		return (x > 0) ? new RGBAShader (1 - (float) x, 1, 1 - (float) x) : new RGBAShader (1, 1 + (float) x, 1 + (float) x);
	}

	/**
	 * Prints <code>v</code> with the specified colour to the XL console.
	 * 
	 * @param v an object
	 * @param color selected printing color (black = 0x000000)
	 */
	public static void print (Object v, int colour)
	{
		Console c = console ();
		if (c != null)
		{
			c.getOut ().print (v, colour);
		}
		else
		{
			System.out.print (v);
		}
	}

	/**
	 * Prints <code>v</code> to the XL console.
	 * 
	 * @param v an object
	 */
	public static void print (Object v)
	{
		Console c = console ();
		if (c != null)
		{
			c.getOut ().print (v, 0x0000c0);
		}
		else
		{
			System.out.print (v);
		}
	}

	/**
	 * Prints <code>v</code> with the specified colour to the XL console, then
	 * terminates the line. 
	 * 
	 * @param v an object
	 * @param color selected printing color (black = 0x000000)
	 */
	public static void println (Object v, int colour)
	{
		Console c = console ();	
		if (c != null)
		{
			c.getOut ().println (v, colour);
		}
		else
		{
			System.out.println (v);
		}
	}

	/**
	 * Prints <code>v</code> to the XL console, then
	 * terminates the line. 
	 * 
	 * @param v an object
	 */
	public static void println (Object v)
	{
		Console c = console ();
		if (c != null)
		{
			c.getOut ().println (v, 0x0000c0);
		}
		else
		{
			System.out.println (v);
		}
	}

	/**
	 * Clears the current console.
	 */
	public static void clearConsole () {
		Console c = console ();
		if(c!=null) c.clear();
	}

	/**
	 * Determine the time for one repaint of the 3D View window.
	 * 
	 * @return time in milliseconds [ms]
	 */
	public static double getTimeForRepaint() {
		long startTime = System.nanoTime();
		View3D.getDefaultView(workbench()).repaint();
		// convert from nanoseconds to milliseconds
		return (System.nanoTime() - startTime) * 1e-6;
	}

	/**
	 * Determine the time for traversing the whole graph (touching each node once).
	 * 
	 * @return time in milliseconds [ms]
	 */
	public static double getTimeForTraversingGraph() {
		DoNothingVisitor v = new DoNothingVisitor();
		v.init(graphState());
		long startTime = System.nanoTime();
		graph().accept(null, v, null);
		// convert from nanoseconds to milliseconds
		return (System.nanoTime() - startTime) * 1e-6;
	}

	/**
	 * Dummy visitor which does nothing to the nodes. 
	 * Used for only determine the time for graph traversing (getTimeForGraphTraversing).
	 * 
	 * @author okn
	 */
	private static class DoNothingVisitor extends DisplayVisitor
			{
		void init (GraphState gs)
		{
			Matrix4d m = new Matrix4d();
			m.setIdentity();
			init (gs, m, null, false);
		}

		@Override
		protected void visitImpl (Object object, boolean asNode, Shader s, Path path)
		{
			//println("Visited " + object);
		}
	}

	/**
	 * Number of nodes in the main graph.
	 * same as count((* Node *))
	 * 
	 * @return int, size of the graph
	 */
	public static int getGraphSize() {
		return workbench().getRegistry ().getProjectGraph ().getGraphSize ();
	}

	/**
	 * Number of nodes in the scene graph.
	 * 	 * same as count((* ShadedNull *))
	 * 
	 * @return int, size of the graph
	 */
	public static int getSceneGraphSize() {
		GraphManager g = workbench().getRegistry ().getProjectGraph ();
		
		HashMap<Node, String> visited = new HashMap<Node, String> ();
		ObjectList<Node> toVisit = new ObjectList<Node> ();
		getListOfNodes (g.getRoot (), null, false, visited, toVisit);
		int counter = 0;
		for(Node item : visited.keySet()) {
			if(item instanceof ShadedNull) counter++;
		}
		return counter;
	}

	/**
	 * 
	 * 
	 * @param node start node
	 * @param edge
	 * @param branch
	 * @param visited list of visited nodes
	 * @param toVisit list of all nodes
	 */
	private static void getListOfNodes (Node node, Edge edge, boolean branch,
			HashMap<Node, String> visited, ObjectList<Node> toVisit)
	{
		String s = visited.get (node);
		if (s == null)
		{
			int inCount = 0;
			int outCount = 0;
			for (Edge e = node.getFirstEdge (); e != null; e = e.getNext (node))
			{
				if (e.isSource (node))
				{
					outCount++;
				}
				else if (e != edge)
				{
					inCount++;
				}
			}
			if (inCount == 0)
			{
				s = "";
			}
			else
			{
				s = "n" + visited.size ();
			}
			visited.put (node, s);
			for (int i = 0; i < 2; i++)
			{
				for (Edge e = node.getFirstEdge (); e != null; e = e.getNext (node))
				{
					if ((i == 0) == (e.getEdgeBits () == Graph.BRANCH_EDGE))
					{
						if (e.isSource (node))
						{
							/*if(node instanceof CfTreeSegment)*/ toVisit.add (node);
							getListOfNodes (e.getTarget (), e, outCount > 0, visited, toVisit);
						}
//						else if ((e != edge) && (visited.get (e.getSource ()) == null))
//						{
//							toVisit.add (e.getSource ());
//						}
					}
				}
			}
		}
	}

	/**
	 * This operator method prints <code>v</code> on <code>w</code>. 
	 * 
	 * @param w a writer
	 * @param v value to be printed on <code>w</code>
	 * @return <code>w</code>
	 */
	public static PrintWriter operator$shl (PrintWriter w, Object v)
	{
		w.print (v);
		return w;
	}

	/**
	 * Terminate the current line by writing the line separator string.
	 */
	public static void println ()
	{
		println ("");
	}

	/*!!
	 #foreach ($type in ["boolean", "char", "int", "long", "float", "double"])
	 $pp.setType($type)

	 /**
	 * Prints <code>v</code> to the XL console.
	 * 
	 * @param v value to be printed
	 $C
	 public static void print ($type v)
	 {
	 print (String.valueOf (v));
	 }


	 /**
	 * Prints <code>v</code> to the XL console, then
	 * terminates the line. 
	 * 
	 * @param v value to be printed
	 $C
	 public static void println ($type v)
	 {
	 println (String.valueOf (v));
	 }
	 
	 /**
	 * This operator method prints <code>v</code> on <code>w</code>. 
	 * 
	 * @param w a writer
	 * @param v value to be printed on <code>w</code>
	 * @return <code>w</code>
	 $C
	 public static PrintWriter operator$shl (PrintWriter w, $type v)
	 {
	 w.print (v);
	 return w;
	 }

	 #end
	 !!*/
//!! #* Start of generated code
	 	 
// generated
	 /**
	 * Prints <code>v</code> to the XL console.
	 * 
	 * @param v value to be printed
	 */
	 public static void print (boolean v)
	 {
	 print (String.valueOf (v));
	 }
// generated
// generated
	 /**
	 * Prints <code>v</code> to the XL console, then
	 * terminates the line. 
	 * 
	 * @param v value to be printed
	 */
	 public static void println (boolean v)
	 {
	 println (String.valueOf (v));
	 }
	 
	 /**
	 * This operator method prints <code>v</code> on <code>w</code>. 
	 * 
	 * @param w a writer
	 * @param v value to be printed on <code>w</code>
	 * @return <code>w</code>
	 */
	 public static PrintWriter operator$shl (PrintWriter w, boolean v)
	 {
	 w.print (v);
	 return w;
	 }
// generated
	 	 
// generated
	 /**
	 * Prints <code>v</code> to the XL console.
	 * 
	 * @param v value to be printed
	 */
	 public static void print (char v)
	 {
	 print (String.valueOf (v));
	 }
// generated
// generated
	 /**
	 * Prints <code>v</code> to the XL console, then
	 * terminates the line. 
	 * 
	 * @param v value to be printed
	 */
	 public static void println (char v)
	 {
	 println (String.valueOf (v));
	 }
	 
	 /**
	 * This operator method prints <code>v</code> on <code>w</code>. 
	 * 
	 * @param w a writer
	 * @param v value to be printed on <code>w</code>
	 * @return <code>w</code>
	 */
	 public static PrintWriter operator$shl (PrintWriter w, char v)
	 {
	 w.print (v);
	 return w;
	 }
// generated
	 	 
// generated
	 /**
	 * Prints <code>v</code> to the XL console.
	 * 
	 * @param v value to be printed
	 */
	 public static void print (int v)
	 {
	 print (String.valueOf (v));
	 }
// generated
// generated
	 /**
	 * Prints <code>v</code> to the XL console, then
	 * terminates the line. 
	 * 
	 * @param v value to be printed
	 */
	 public static void println (int v)
	 {
	 println (String.valueOf (v));
	 }
	 
	 /**
	 * This operator method prints <code>v</code> on <code>w</code>. 
	 * 
	 * @param w a writer
	 * @param v value to be printed on <code>w</code>
	 * @return <code>w</code>
	 */
	 public static PrintWriter operator$shl (PrintWriter w, int v)
	 {
	 w.print (v);
	 return w;
	 }
// generated
	 	 
// generated
	 /**
	 * Prints <code>v</code> to the XL console.
	 * 
	 * @param v value to be printed
	 */
	 public static void print (long v)
	 {
	 print (String.valueOf (v));
	 }
// generated
// generated
	 /**
	 * Prints <code>v</code> to the XL console, then
	 * terminates the line. 
	 * 
	 * @param v value to be printed
	 */
	 public static void println (long v)
	 {
	 println (String.valueOf (v));
	 }
	 
	 /**
	 * This operator method prints <code>v</code> on <code>w</code>. 
	 * 
	 * @param w a writer
	 * @param v value to be printed on <code>w</code>
	 * @return <code>w</code>
	 */
	 public static PrintWriter operator$shl (PrintWriter w, long v)
	 {
	 w.print (v);
	 return w;
	 }
// generated
	 	 
// generated
	 /**
	 * Prints <code>v</code> to the XL console.
	 * 
	 * @param v value to be printed
	 */
	 public static void print (float v)
	 {
	 print (String.valueOf (v));
	 }
// generated
// generated
	 /**
	 * Prints <code>v</code> to the XL console, then
	 * terminates the line. 
	 * 
	 * @param v value to be printed
	 */
	 public static void println (float v)
	 {
	 println (String.valueOf (v));
	 }
	 
	 /**
	 * This operator method prints <code>v</code> on <code>w</code>. 
	 * 
	 * @param w a writer
	 * @param v value to be printed on <code>w</code>
	 * @return <code>w</code>
	 */
	 public static PrintWriter operator$shl (PrintWriter w, float v)
	 {
	 w.print (v);
	 return w;
	 }
// generated
	 	 
// generated
	 /**
	 * Prints <code>v</code> to the XL console.
	 * 
	 * @param v value to be printed
	 */
	 public static void print (double v)
	 {
	 print (String.valueOf (v));
	 }
// generated
// generated
	 /**
	 * Prints <code>v</code> to the XL console, then
	 * terminates the line. 
	 * 
	 * @param v value to be printed
	 */
	 public static void println (double v)
	 {
	 println (String.valueOf (v));
	 }
	 
	 /**
	 * This operator method prints <code>v</code> on <code>w</code>. 
	 * 
	 * @param w a writer
	 * @param v value to be printed on <code>w</code>
	 * @return <code>w</code>
	 */
	 public static PrintWriter operator$shl (PrintWriter w, double v)
	 {
	 w.print (v);
	 return w;
	 }
// generated
//!! *# End of generated code

	/**
	 * This operator method is an alias for <code>func.evaluateFloat(x)</code>.
	 * 
	 * @param func a function
	 * @param x argument to the function
	 * @return evaluation of <code>func</code> at <code>x</code>
	 */
	public static float operator$index (FloatToFloat func, float x)
	{
		return func.evaluateFloat (x);
	}

	/**
	 * This operator method adds <code>value</code> as last element to
	 * <code>list</code>.
	 * 
	 * @param list a list
	 * @param value value to add as last element
	 * @return <code>list</code>
	 */
	public static <E> List<E> operator$shl (List<E> list, E value)
	{
		list.add (value);
		return list;
	}

	public static <T> T operator$index (Node node, ObjectAttribute<T> attr)
	{
		return node.getCurrentGraphState ().getObject (node, true, attr);
	}

	/**
	 * This operator method returns <code>true</code> iff
	 * <code>set</code> is not <code>null</code> and
	 * <code>set.contains(value)</code> returns <code>true</code>.
	 * 
	 * @param value a value
	 * @param set a collection
	 * @return <code>true</code> iff <code>value</code> is in <code>set</code>
	 */
	public static <T> boolean operator$in (T value, Collection<? super T> set)
	{
		return (set != null) && set.contains (value);
	}

	/*!!
	 #foreach ($type in $types)
	 $pp.setType($type)

	 #if ($pp.object)
	 #set ($extSig = ",V")
	 #set ($genType = "V")
	 #set ($listSig = "<V>")
	 #else
	 #set ($extSig = "")
	 #set ($genType = $type)
	 #set ($listSig = "")
	 #end

	 /**
	 * This method can be used to compute the values of a synthesized
	 * attribute for every node of a tree-like structure. A synthesized
	 * attribute is an attribute for a node whose value depends on the
	 * node and its descendants. <code>root</code> defines the root of
	 * the tree-like structure, <code>generator</code> is used to
	 * obtain the children of a node, and <code>synth</code> to compute
	 * the value of the synthesized attribute at the current node,
	 * where the values of its children have been computed previously
	 * and are passed to <code>synth</code>.
	 * 
	 * @param <T> the type of nodes
	 #if ($pp.object)
	 * @param <V> the type of values of the synthesized attribute
	 #end
	 * @param root root node of structure
	 * @param generator the generator is used to obtain the direct children
	 * of the current node
	 * @param synth this function is used to compute the value of the
	 * synthesized attribute at a node, given the values of its children
	 * @return value of synthesized attribute at <code>root</code>
	 $C
	 public static <T${extSig}> $genType synthesize (T root,
	 ObjectToObjectGenerator<? super T, ? extends T> generator,
	 #if ($pp.object)
	 ObjectToObject<ObjectSynth<? super T,? super V>,? extends V>
	 #else
	 ObjectTo${pp.Type}<${pp.Type}Synth<? super T>>
	 #end
	 synth)
	 {
	 ObjectList<Object> stack = new ObjectList<Object> (100);
	 ${pp.Type}List${listSig} values = new ${pp.Type}List${listSig} ();
	 stack.push (root);
	 ${pp.Type}Synth<T${extSig}> syn = new ${pp.Type}Synth<T${extSig}> ();
	 syn.valuesList = values;
	 while (!stack.isEmpty ())
	 {
	 Object o = stack.peek (1);
	 if (o instanceof Node)
	 {
	 stack.push (null);
	 int s = stack.size ();
	 generator.evaluateObject (stack, (T) o);
	 stack.set (s - 1, Integer.valueOf (stack.size () - s));
	 }
	 else
	 {
	 stack.pop ();
	 syn.object = (T) stack.pop ();
	 syn.startIndex = values.size - (Integer) o;
	 $genType v = synth.evaluate$pp.Type (syn);
	 values.setSize (syn.startIndex);
	 values.add (v);
	 }
	 }
	 assert values.size == 1;
	 return values.pop ();
	 }

	 #end
	 !!*/
//!! #* Start of generated code
	 	 
// generated
	 	 
	 /**
	 * This method can be used to compute the values of a synthesized
	 * attribute for every node of a tree-like structure. A synthesized
	 * attribute is an attribute for a node whose value depends on the
	 * node and its descendants. <code>root</code> defines the root of
	 * the tree-like structure, <code>generator</code> is used to
	 * obtain the children of a node, and <code>synth</code> to compute
	 * the value of the synthesized attribute at the current node,
	 * where the values of its children have been computed previously
	 * and are passed to <code>synth</code>.
	 * 
	 * @param <T> the type of nodes
	 	 * @param root root node of structure
	 * @param generator the generator is used to obtain the direct children
	 * of the current node
	 * @param synth this function is used to compute the value of the
	 * synthesized attribute at a node, given the values of its children
	 * @return value of synthesized attribute at <code>root</code>
	 */
	 public static <T> boolean synthesize (T root,
	 ObjectToObjectGenerator<? super T, ? extends T> generator,
	 	 ObjectToBoolean<BooleanSynth<? super T>>
	 	 synth)
	 {
	 ObjectList<Object> stack = new ObjectList<Object> (100);
	 BooleanList values = new BooleanList ();
	 stack.push (root);
	 BooleanSynth<T> syn = new BooleanSynth<T> ();
	 syn.valuesList = values;
	 while (!stack.isEmpty ())
	 {
	 Object o = stack.peek (1);
	 if (o instanceof Node)
	 {
	 stack.push (null);
	 int s = stack.size ();
	 generator.evaluateObject (stack, (T) o);
	 stack.set (s - 1, Integer.valueOf (stack.size () - s));
	 }
	 else
	 {
	 stack.pop ();
	 syn.object = (T) stack.pop ();
	 syn.startIndex = values.size - (Integer) o;
	 boolean v = synth.evaluateBoolean (syn);
	 values.setSize (syn.startIndex);
	 values.add (v);
	 }
	 }
	 assert values.size == 1;
	 return values.pop ();
	 }
// generated
	 	 
// generated
	 	 
	 /**
	 * This method can be used to compute the values of a synthesized
	 * attribute for every node of a tree-like structure. A synthesized
	 * attribute is an attribute for a node whose value depends on the
	 * node and its descendants. <code>root</code> defines the root of
	 * the tree-like structure, <code>generator</code> is used to
	 * obtain the children of a node, and <code>synth</code> to compute
	 * the value of the synthesized attribute at the current node,
	 * where the values of its children have been computed previously
	 * and are passed to <code>synth</code>.
	 * 
	 * @param <T> the type of nodes
	 	 * @param root root node of structure
	 * @param generator the generator is used to obtain the direct children
	 * of the current node
	 * @param synth this function is used to compute the value of the
	 * synthesized attribute at a node, given the values of its children
	 * @return value of synthesized attribute at <code>root</code>
	 */
	 public static <T> byte synthesize (T root,
	 ObjectToObjectGenerator<? super T, ? extends T> generator,
	 	 ObjectToByte<ByteSynth<? super T>>
	 	 synth)
	 {
	 ObjectList<Object> stack = new ObjectList<Object> (100);
	 ByteList values = new ByteList ();
	 stack.push (root);
	 ByteSynth<T> syn = new ByteSynth<T> ();
	 syn.valuesList = values;
	 while (!stack.isEmpty ())
	 {
	 Object o = stack.peek (1);
	 if (o instanceof Node)
	 {
	 stack.push (null);
	 int s = stack.size ();
	 generator.evaluateObject (stack, (T) o);
	 stack.set (s - 1, Integer.valueOf (stack.size () - s));
	 }
	 else
	 {
	 stack.pop ();
	 syn.object = (T) stack.pop ();
	 syn.startIndex = values.size - (Integer) o;
	 byte v = synth.evaluateByte (syn);
	 values.setSize (syn.startIndex);
	 values.add (v);
	 }
	 }
	 assert values.size == 1;
	 return values.pop ();
	 }
// generated
	 	 
// generated
	 	 
	 /**
	 * This method can be used to compute the values of a synthesized
	 * attribute for every node of a tree-like structure. A synthesized
	 * attribute is an attribute for a node whose value depends on the
	 * node and its descendants. <code>root</code> defines the root of
	 * the tree-like structure, <code>generator</code> is used to
	 * obtain the children of a node, and <code>synth</code> to compute
	 * the value of the synthesized attribute at the current node,
	 * where the values of its children have been computed previously
	 * and are passed to <code>synth</code>.
	 * 
	 * @param <T> the type of nodes
	 	 * @param root root node of structure
	 * @param generator the generator is used to obtain the direct children
	 * of the current node
	 * @param synth this function is used to compute the value of the
	 * synthesized attribute at a node, given the values of its children
	 * @return value of synthesized attribute at <code>root</code>
	 */
	 public static <T> short synthesize (T root,
	 ObjectToObjectGenerator<? super T, ? extends T> generator,
	 	 ObjectToShort<ShortSynth<? super T>>
	 	 synth)
	 {
	 ObjectList<Object> stack = new ObjectList<Object> (100);
	 ShortList values = new ShortList ();
	 stack.push (root);
	 ShortSynth<T> syn = new ShortSynth<T> ();
	 syn.valuesList = values;
	 while (!stack.isEmpty ())
	 {
	 Object o = stack.peek (1);
	 if (o instanceof Node)
	 {
	 stack.push (null);
	 int s = stack.size ();
	 generator.evaluateObject (stack, (T) o);
	 stack.set (s - 1, Integer.valueOf (stack.size () - s));
	 }
	 else
	 {
	 stack.pop ();
	 syn.object = (T) stack.pop ();
	 syn.startIndex = values.size - (Integer) o;
	 short v = synth.evaluateShort (syn);
	 values.setSize (syn.startIndex);
	 values.add (v);
	 }
	 }
	 assert values.size == 1;
	 return values.pop ();
	 }
// generated
	 	 
// generated
	 	 
	 /**
	 * This method can be used to compute the values of a synthesized
	 * attribute for every node of a tree-like structure. A synthesized
	 * attribute is an attribute for a node whose value depends on the
	 * node and its descendants. <code>root</code> defines the root of
	 * the tree-like structure, <code>generator</code> is used to
	 * obtain the children of a node, and <code>synth</code> to compute
	 * the value of the synthesized attribute at the current node,
	 * where the values of its children have been computed previously
	 * and are passed to <code>synth</code>.
	 * 
	 * @param <T> the type of nodes
	 	 * @param root root node of structure
	 * @param generator the generator is used to obtain the direct children
	 * of the current node
	 * @param synth this function is used to compute the value of the
	 * synthesized attribute at a node, given the values of its children
	 * @return value of synthesized attribute at <code>root</code>
	 */
	 public static <T> char synthesize (T root,
	 ObjectToObjectGenerator<? super T, ? extends T> generator,
	 	 ObjectToChar<CharSynth<? super T>>
	 	 synth)
	 {
	 ObjectList<Object> stack = new ObjectList<Object> (100);
	 CharList values = new CharList ();
	 stack.push (root);
	 CharSynth<T> syn = new CharSynth<T> ();
	 syn.valuesList = values;
	 while (!stack.isEmpty ())
	 {
	 Object o = stack.peek (1);
	 if (o instanceof Node)
	 {
	 stack.push (null);
	 int s = stack.size ();
	 generator.evaluateObject (stack, (T) o);
	 stack.set (s - 1, Integer.valueOf (stack.size () - s));
	 }
	 else
	 {
	 stack.pop ();
	 syn.object = (T) stack.pop ();
	 syn.startIndex = values.size - (Integer) o;
	 char v = synth.evaluateChar (syn);
	 values.setSize (syn.startIndex);
	 values.add (v);
	 }
	 }
	 assert values.size == 1;
	 return values.pop ();
	 }
// generated
	 	 
// generated
	 	 
	 /**
	 * This method can be used to compute the values of a synthesized
	 * attribute for every node of a tree-like structure. A synthesized
	 * attribute is an attribute for a node whose value depends on the
	 * node and its descendants. <code>root</code> defines the root of
	 * the tree-like structure, <code>generator</code> is used to
	 * obtain the children of a node, and <code>synth</code> to compute
	 * the value of the synthesized attribute at the current node,
	 * where the values of its children have been computed previously
	 * and are passed to <code>synth</code>.
	 * 
	 * @param <T> the type of nodes
	 	 * @param root root node of structure
	 * @param generator the generator is used to obtain the direct children
	 * of the current node
	 * @param synth this function is used to compute the value of the
	 * synthesized attribute at a node, given the values of its children
	 * @return value of synthesized attribute at <code>root</code>
	 */
	 public static <T> int synthesize (T root,
	 ObjectToObjectGenerator<? super T, ? extends T> generator,
	 	 ObjectToInt<IntSynth<? super T>>
	 	 synth)
	 {
	 ObjectList<Object> stack = new ObjectList<Object> (100);
	 IntList values = new IntList ();
	 stack.push (root);
	 IntSynth<T> syn = new IntSynth<T> ();
	 syn.valuesList = values;
	 while (!stack.isEmpty ())
	 {
	 Object o = stack.peek (1);
	 if (o instanceof Node)
	 {
	 stack.push (null);
	 int s = stack.size ();
	 generator.evaluateObject (stack, (T) o);
	 stack.set (s - 1, Integer.valueOf (stack.size () - s));
	 }
	 else
	 {
	 stack.pop ();
	 syn.object = (T) stack.pop ();
	 syn.startIndex = values.size - (Integer) o;
	 int v = synth.evaluateInt (syn);
	 values.setSize (syn.startIndex);
	 values.add (v);
	 }
	 }
	 assert values.size == 1;
	 return values.pop ();
	 }
// generated
	 	 
// generated
	 	 
	 /**
	 * This method can be used to compute the values of a synthesized
	 * attribute for every node of a tree-like structure. A synthesized
	 * attribute is an attribute for a node whose value depends on the
	 * node and its descendants. <code>root</code> defines the root of
	 * the tree-like structure, <code>generator</code> is used to
	 * obtain the children of a node, and <code>synth</code> to compute
	 * the value of the synthesized attribute at the current node,
	 * where the values of its children have been computed previously
	 * and are passed to <code>synth</code>.
	 * 
	 * @param <T> the type of nodes
	 	 * @param root root node of structure
	 * @param generator the generator is used to obtain the direct children
	 * of the current node
	 * @param synth this function is used to compute the value of the
	 * synthesized attribute at a node, given the values of its children
	 * @return value of synthesized attribute at <code>root</code>
	 */
	 public static <T> long synthesize (T root,
	 ObjectToObjectGenerator<? super T, ? extends T> generator,
	 	 ObjectToLong<LongSynth<? super T>>
	 	 synth)
	 {
	 ObjectList<Object> stack = new ObjectList<Object> (100);
	 LongList values = new LongList ();
	 stack.push (root);
	 LongSynth<T> syn = new LongSynth<T> ();
	 syn.valuesList = values;
	 while (!stack.isEmpty ())
	 {
	 Object o = stack.peek (1);
	 if (o instanceof Node)
	 {
	 stack.push (null);
	 int s = stack.size ();
	 generator.evaluateObject (stack, (T) o);
	 stack.set (s - 1, Integer.valueOf (stack.size () - s));
	 }
	 else
	 {
	 stack.pop ();
	 syn.object = (T) stack.pop ();
	 syn.startIndex = values.size - (Integer) o;
	 long v = synth.evaluateLong (syn);
	 values.setSize (syn.startIndex);
	 values.add (v);
	 }
	 }
	 assert values.size == 1;
	 return values.pop ();
	 }
// generated
	 	 
// generated
	 	 
	 /**
	 * This method can be used to compute the values of a synthesized
	 * attribute for every node of a tree-like structure. A synthesized
	 * attribute is an attribute for a node whose value depends on the
	 * node and its descendants. <code>root</code> defines the root of
	 * the tree-like structure, <code>generator</code> is used to
	 * obtain the children of a node, and <code>synth</code> to compute
	 * the value of the synthesized attribute at the current node,
	 * where the values of its children have been computed previously
	 * and are passed to <code>synth</code>.
	 * 
	 * @param <T> the type of nodes
	 	 * @param root root node of structure
	 * @param generator the generator is used to obtain the direct children
	 * of the current node
	 * @param synth this function is used to compute the value of the
	 * synthesized attribute at a node, given the values of its children
	 * @return value of synthesized attribute at <code>root</code>
	 */
	 public static <T> float synthesize (T root,
	 ObjectToObjectGenerator<? super T, ? extends T> generator,
	 	 ObjectToFloat<FloatSynth<? super T>>
	 	 synth)
	 {
	 ObjectList<Object> stack = new ObjectList<Object> (100);
	 FloatList values = new FloatList ();
	 stack.push (root);
	 FloatSynth<T> syn = new FloatSynth<T> ();
	 syn.valuesList = values;
	 while (!stack.isEmpty ())
	 {
	 Object o = stack.peek (1);
	 if (o instanceof Node)
	 {
	 stack.push (null);
	 int s = stack.size ();
	 generator.evaluateObject (stack, (T) o);
	 stack.set (s - 1, Integer.valueOf (stack.size () - s));
	 }
	 else
	 {
	 stack.pop ();
	 syn.object = (T) stack.pop ();
	 syn.startIndex = values.size - (Integer) o;
	 float v = synth.evaluateFloat (syn);
	 values.setSize (syn.startIndex);
	 values.add (v);
	 }
	 }
	 assert values.size == 1;
	 return values.pop ();
	 }
// generated
	 	 
// generated
	 	 
	 /**
	 * This method can be used to compute the values of a synthesized
	 * attribute for every node of a tree-like structure. A synthesized
	 * attribute is an attribute for a node whose value depends on the
	 * node and its descendants. <code>root</code> defines the root of
	 * the tree-like structure, <code>generator</code> is used to
	 * obtain the children of a node, and <code>synth</code> to compute
	 * the value of the synthesized attribute at the current node,
	 * where the values of its children have been computed previously
	 * and are passed to <code>synth</code>.
	 * 
	 * @param <T> the type of nodes
	 	 * @param root root node of structure
	 * @param generator the generator is used to obtain the direct children
	 * of the current node
	 * @param synth this function is used to compute the value of the
	 * synthesized attribute at a node, given the values of its children
	 * @return value of synthesized attribute at <code>root</code>
	 */
	 public static <T> double synthesize (T root,
	 ObjectToObjectGenerator<? super T, ? extends T> generator,
	 	 ObjectToDouble<DoubleSynth<? super T>>
	 	 synth)
	 {
	 ObjectList<Object> stack = new ObjectList<Object> (100);
	 DoubleList values = new DoubleList ();
	 stack.push (root);
	 DoubleSynth<T> syn = new DoubleSynth<T> ();
	 syn.valuesList = values;
	 while (!stack.isEmpty ())
	 {
	 Object o = stack.peek (1);
	 if (o instanceof Node)
	 {
	 stack.push (null);
	 int s = stack.size ();
	 generator.evaluateObject (stack, (T) o);
	 stack.set (s - 1, Integer.valueOf (stack.size () - s));
	 }
	 else
	 {
	 stack.pop ();
	 syn.object = (T) stack.pop ();
	 syn.startIndex = values.size - (Integer) o;
	 double v = synth.evaluateDouble (syn);
	 values.setSize (syn.startIndex);
	 values.add (v);
	 }
	 }
	 assert values.size == 1;
	 return values.pop ();
	 }
// generated
	 	 
// generated
	 	 
	 /**
	 * This method can be used to compute the values of a synthesized
	 * attribute for every node of a tree-like structure. A synthesized
	 * attribute is an attribute for a node whose value depends on the
	 * node and its descendants. <code>root</code> defines the root of
	 * the tree-like structure, <code>generator</code> is used to
	 * obtain the children of a node, and <code>synth</code> to compute
	 * the value of the synthesized attribute at the current node,
	 * where the values of its children have been computed previously
	 * and are passed to <code>synth</code>.
	 * 
	 * @param <T> the type of nodes
	 	 * @param <V> the type of values of the synthesized attribute
	 	 * @param root root node of structure
	 * @param generator the generator is used to obtain the direct children
	 * of the current node
	 * @param synth this function is used to compute the value of the
	 * synthesized attribute at a node, given the values of its children
	 * @return value of synthesized attribute at <code>root</code>
	 */
	 public static <T,V> V synthesize (T root,
	 ObjectToObjectGenerator<? super T, ? extends T> generator,
	 	 ObjectToObject<ObjectSynth<? super T,? super V>,? extends V>
	 	 synth)
	 {
	 ObjectList<Object> stack = new ObjectList<Object> (100);
	 ObjectList<V> values = new ObjectList<V> ();
	 stack.push (root);
	 ObjectSynth<T,V> syn = new ObjectSynth<T,V> ();
	 syn.valuesList = values;
	 while (!stack.isEmpty ())
	 {
	 Object o = stack.peek (1);
	 if (o instanceof Node)
	 {
	 stack.push (null);
	 int s = stack.size ();
	 generator.evaluateObject (stack, (T) o);
	 stack.set (s - 1, Integer.valueOf (stack.size () - s));
	 }
	 else
	 {
	 stack.pop ();
	 syn.object = (T) stack.pop ();
	 syn.startIndex = values.size - (Integer) o;
	 V v = synth.evaluateObject (syn);
	 values.setSize (syn.startIndex);
	 values.add (v);
	 }
	 }
	 assert values.size == 1;
	 return values.pop ();
	 }
// generated
//!! *# End of generated code

	/*!!
	 #foreach ($type in $primitives)
	 $pp.setType($type)

	 /**
	 * This operator method is an alias for <code>list.get(index)</code>.
	 * 
	 * @param list a list
	 * @param index an index
	 * @return element at <code>index</code> in <code>list</code>
	 $C
	 public static $type operator$index (${pp.Type}List list, int index)
	 {
	 	return list.get (index);
	 }

	 /**
	 * This operator method adds <code>value</code> as last element to
	 * <code>list</code>.
	 * 
	 * @param list a list
	 * @param value value to add as last element
	 * @return <code>list</code>
	 $C
	 public static ${pp.Type}List operator$shl (${pp.Type}List list, $type value)
	 {
	 	list.add (value);
	 	return list;
	 }

	 public static $type operator$index (Node node, ${pp.Type}Attribute attr)
	 {
		return node.getCurrentGraphState ().get$pp.Type (node, true, attr);
	 }

	 #end
	 !!*/
//!! #* Start of generated code
	 	 
// generated
	 /**
	 * This operator method is an alias for <code>list.get(index)</code>.
	 * 
	 * @param list a list
	 * @param index an index
	 * @return element at <code>index</code> in <code>list</code>
	 */
	 public static boolean operator$index (BooleanList list, int index)
	 {
	 	return list.get (index);
	 }
// generated
	 /**
	 * This operator method adds <code>value</code> as last element to
	 * <code>list</code>.
	 * 
	 * @param list a list
	 * @param value value to add as last element
	 * @return <code>list</code>
	 */
	 public static BooleanList operator$shl (BooleanList list, boolean value)
	 {
	 	list.add (value);
	 	return list;
	 }
// generated
	 public static boolean operator$index (Node node, BooleanAttribute attr)
	 {
		return node.getCurrentGraphState ().getBoolean (node, true, attr);
	 }
// generated
	 	 
// generated
	 /**
	 * This operator method is an alias for <code>list.get(index)</code>.
	 * 
	 * @param list a list
	 * @param index an index
	 * @return element at <code>index</code> in <code>list</code>
	 */
	 public static byte operator$index (ByteList list, int index)
	 {
	 	return list.get (index);
	 }
// generated
	 /**
	 * This operator method adds <code>value</code> as last element to
	 * <code>list</code>.
	 * 
	 * @param list a list
	 * @param value value to add as last element
	 * @return <code>list</code>
	 */
	 public static ByteList operator$shl (ByteList list, byte value)
	 {
	 	list.add (value);
	 	return list;
	 }
// generated
	 public static byte operator$index (Node node, ByteAttribute attr)
	 {
		return node.getCurrentGraphState ().getByte (node, true, attr);
	 }
// generated
	 	 
// generated
	 /**
	 * This operator method is an alias for <code>list.get(index)</code>.
	 * 
	 * @param list a list
	 * @param index an index
	 * @return element at <code>index</code> in <code>list</code>
	 */
	 public static short operator$index (ShortList list, int index)
	 {
	 	return list.get (index);
	 }
// generated
	 /**
	 * This operator method adds <code>value</code> as last element to
	 * <code>list</code>.
	 * 
	 * @param list a list
	 * @param value value to add as last element
	 * @return <code>list</code>
	 */
	 public static ShortList operator$shl (ShortList list, short value)
	 {
	 	list.add (value);
	 	return list;
	 }
// generated
	 public static short operator$index (Node node, ShortAttribute attr)
	 {
		return node.getCurrentGraphState ().getShort (node, true, attr);
	 }
// generated
	 	 
// generated
	 /**
	 * This operator method is an alias for <code>list.get(index)</code>.
	 * 
	 * @param list a list
	 * @param index an index
	 * @return element at <code>index</code> in <code>list</code>
	 */
	 public static char operator$index (CharList list, int index)
	 {
	 	return list.get (index);
	 }
// generated
	 /**
	 * This operator method adds <code>value</code> as last element to
	 * <code>list</code>.
	 * 
	 * @param list a list
	 * @param value value to add as last element
	 * @return <code>list</code>
	 */
	 public static CharList operator$shl (CharList list, char value)
	 {
	 	list.add (value);
	 	return list;
	 }
// generated
	 public static char operator$index (Node node, CharAttribute attr)
	 {
		return node.getCurrentGraphState ().getChar (node, true, attr);
	 }
// generated
	 	 
// generated
	 /**
	 * This operator method is an alias for <code>list.get(index)</code>.
	 * 
	 * @param list a list
	 * @param index an index
	 * @return element at <code>index</code> in <code>list</code>
	 */
	 public static int operator$index (IntList list, int index)
	 {
	 	return list.get (index);
	 }
// generated
	 /**
	 * This operator method adds <code>value</code> as last element to
	 * <code>list</code>.
	 * 
	 * @param list a list
	 * @param value value to add as last element
	 * @return <code>list</code>
	 */
	 public static IntList operator$shl (IntList list, int value)
	 {
	 	list.add (value);
	 	return list;
	 }
// generated
	 public static int operator$index (Node node, IntAttribute attr)
	 {
		return node.getCurrentGraphState ().getInt (node, true, attr);
	 }
// generated
	 	 
// generated
	 /**
	 * This operator method is an alias for <code>list.get(index)</code>.
	 * 
	 * @param list a list
	 * @param index an index
	 * @return element at <code>index</code> in <code>list</code>
	 */
	 public static long operator$index (LongList list, int index)
	 {
	 	return list.get (index);
	 }
// generated
	 /**
	 * This operator method adds <code>value</code> as last element to
	 * <code>list</code>.
	 * 
	 * @param list a list
	 * @param value value to add as last element
	 * @return <code>list</code>
	 */
	 public static LongList operator$shl (LongList list, long value)
	 {
	 	list.add (value);
	 	return list;
	 }
// generated
	 public static long operator$index (Node node, LongAttribute attr)
	 {
		return node.getCurrentGraphState ().getLong (node, true, attr);
	 }
// generated
	 	 
// generated
	 /**
	 * This operator method is an alias for <code>list.get(index)</code>.
	 * 
	 * @param list a list
	 * @param index an index
	 * @return element at <code>index</code> in <code>list</code>
	 */
	 public static float operator$index (FloatList list, int index)
	 {
	 	return list.get (index);
	 }
// generated
	 /**
	 * This operator method adds <code>value</code> as last element to
	 * <code>list</code>.
	 * 
	 * @param list a list
	 * @param value value to add as last element
	 * @return <code>list</code>
	 */
	 public static FloatList operator$shl (FloatList list, float value)
	 {
	 	list.add (value);
	 	return list;
	 }
// generated
	 public static float operator$index (Node node, FloatAttribute attr)
	 {
		return node.getCurrentGraphState ().getFloat (node, true, attr);
	 }
// generated
	 	 
// generated
	 /**
	 * This operator method is an alias for <code>list.get(index)</code>.
	 * 
	 * @param list a list
	 * @param index an index
	 * @return element at <code>index</code> in <code>list</code>
	 */
	 public static double operator$index (DoubleList list, int index)
	 {
	 	return list.get (index);
	 }
// generated
	 /**
	 * This operator method adds <code>value</code> as last element to
	 * <code>list</code>.
	 * 
	 * @param list a list
	 * @param value value to add as last element
	 * @return <code>list</code>
	 */
	 public static DoubleList operator$shl (DoubleList list, double value)
	 {
	 	list.add (value);
	 	return list;
	 }
// generated
	 public static double operator$index (Node node, DoubleAttribute attr)
	 {
		return node.getCurrentGraphState ().getDouble (node, true, attr);
	 }
// generated
//!! *# End of generated code

	/**
	 * This operator method is an alias for <code>node.getChild(index)</code>.
	 * 
	 * @param node a node
	 * @param index an index
	 * @return <code>index</code>-th child of <code>code</code>
	 * 
	 * @see Node#getBranchNode(int)
	 */
	public static Node operator$index (Node node, int index)
	{
		return node.getBranchNode (index);
	}

	/**
	 * This operator method compares two nodes based on their id:
	 * It returns <code>true</code> iff
	 * <code>a.getId () < b.getId ()</code>.
	 * 
	 * @param a a node
	 * @param b another node
	 * @return <code>a.getId () < b.getId ()</code>
	 * 
	 * @see Node#getId()
	 */
	public static boolean operator$lt (Node a, Node b)
	{
		return a.getId () < b.getId ();
	}

	/**
	 * This operator method compares two nodes based on their id:
	 * It returns <code>true</code> iff
	 * <code>a.getId () <= b.getId ()</code>.
	 * 
	 * @param a a node
	 * @param b another node
	 * @return <code>a.getId () <= b.getId ()</code>
	 * 
	 * @see Node#getId()
	 */
	public static boolean operator$le (Node a, Node b)
	{
		return a.getId () <= b.getId ();
	}

	/**
	 * This operator method compares two nodes based on their id:
	 * It returns <code>true</code> iff
	 * <code>a.getId () > b.getId ()</code>.
	 * 
	 * @param a a node
	 * @param b another node
	 * @return <code>a.getId () > b.getId ()</code>
	 * 
	 * @see Node#getId()
	 */
	public static boolean operator$gt (Node a, Node b)
	{
		return a.getId () > b.getId ();
	}

	/**
	 * This operator method compares two nodes based on their id:
	 * It returns <code>true</code> iff
	 * <code>a.getId () >= b.getId ()</code>.
	 * 
	 * @param a a node
	 * @param b another node
	 * @return <code>a.getId () >= b.getId ()</code>
	 * 
	 * @see Node#getId()
	 */
	public static boolean operator$ge (Node a, Node b)
	{
		return a.getId () >= b.getId ();
	}

	/*!!
	 #foreach ($type in ["int", "float", "double"])
	 $pp.setType($type)

	 /**
	 * This operator method applies the function
	 * <code>f</code> to node <code>n</code>. It is an alias for
	 * <code>f.get(n)</code>.
	 * 
	 * @param n a node
	 * @param f a function of nodes
	 * @return evaluation of <code>f</code> at <code>n</code>
	 $C
	 public static $type operator$index (Node n, NodeTo${pp.Type} f)
	 {
	 return f.evaluate${pp.Type} (n);
	 }

	 #end
	 !!*/
//!! #* Start of generated code
	 	 
// generated
	 /**
	 * This operator method applies the function
	 * <code>f</code> to node <code>n</code>. It is an alias for
	 * <code>f.get(n)</code>.
	 * 
	 * @param n a node
	 * @param f a function of nodes
	 * @return evaluation of <code>f</code> at <code>n</code>
	 */
	 public static int operator$index (Node n, NodeToInt f)
	 {
	 return f.evaluateInt (n);
	 }
// generated
	 	 
// generated
	 /**
	 * This operator method applies the function
	 * <code>f</code> to node <code>n</code>. It is an alias for
	 * <code>f.get(n)</code>.
	 * 
	 * @param n a node
	 * @param f a function of nodes
	 * @return evaluation of <code>f</code> at <code>n</code>
	 */
	 public static float operator$index (Node n, NodeToFloat f)
	 {
	 return f.evaluateFloat (n);
	 }
// generated
	 	 
// generated
	 /**
	 * This operator method applies the function
	 * <code>f</code> to node <code>n</code>. It is an alias for
	 * <code>f.get(n)</code>.
	 * 
	 * @param n a node
	 * @param f a function of nodes
	 * @return evaluation of <code>f</code> at <code>n</code>
	 */
	 public static double operator$index (Node n, NodeToDouble f)
	 {
	 return f.evaluateDouble (n);
	 }
// generated
//!! *# End of generated code

	/**
	 * This operator method returns the location of <code>n</code>.
	 * It is an alias for <code>location(n)</code>. 
	 * 
	 * @param n a node
	 * @param l dummy parameter, its value is ignored
	 * @return location of <code>n</code> in global coordinates
	 * 
	 * @see #location(Node)
	 */
	public static Point3d operator$index (Node n, Location l)
	{
		return location (n);
	}

	/**
	 * This aggregate method computes the mean of a sequence of
	 * <code>Tuple3f</code>'s.
	 * 
	 * @param a aggregate instance (provided by the XL compiler)
	 * @param value a value of the sequence of values
	 */
	public static void mean (Aggregate a, Tuple3f value)
	{
		if (a.initialize ())
		{
			try
			{
				a.aval = a.getType ().newInstance ();
			}
			catch (Exception e)
			{
				throw new WrapException (e);
			}
			((Tuple3f) a.aval).set (0, 0, 0);
			a.ival = 0;
		}
		if (a.isFinished ())
		{
			if (a.ival > 1)
			{
				((Tuple3f) a.aval).scale (1f / a.ival);
			}
		}
		else
		{
			((Tuple3f) a.aval).add (value);
			a.ival++;
		}
	}

	/**
	 * This aggregate method computes the mean of a sequence of
	 * <code>Tuple3d</code>'s.
	 * 
	 * @param a aggregate instance (provided by the XL compiler)
	 * @param value a value of the sequence of values
	 */
	public static void mean (Aggregate a, Tuple3d value)
	{
		if (a.initialize ())
		{
			try
			{
				a.aval = a.getType ().newInstance ();
			}
			catch (Exception e)
			{
				throw new WrapException (e);
			}
			((Tuple3d) a.aval).set (0, 0, 0);
			a.ival = 0;
		}
		if (a.isFinished ())
		{
			if (a.ival > 1)
			{
				((Tuple3d) a.aval).scale (1d / a.ival);
			}
		}
		else
		{
			((Tuple3d) a.aval).add (value);
			a.ival++;
		}
	}

	public static boolean plot (Aggregate a, IntToDouble function, int x)
	{
		if (a.initialize ())
		{
			DatasetRef data = new DatasetRef ("plotData");
			data.clear ().addColumn ("Values");
			a.aval1 = data;
			a.ival = 0;
		}
		if (a.isFinished ())
		{
			chart ((DatasetRef) a.aval1, ChartPanel.BAR_PLOT);
		}
		else
		{
			((DatasetRef) a.aval1).addRow (x).set (0,
				function.evaluateDouble (x));
		}
		return false;
	}

	public static boolean plot (Aggregate a, DoubleToDouble function, double x)
	{
		if (a.initialize ())
		{
			DatasetRef data = new DatasetRef ("plotData");
			data.clear ().addColumn ("Values");
			a.aval1 = data;
			a.ival = 0;
		}
		if (a.isFinished ())
		{
			chart ((DatasetRef) a.aval1, ChartPanel.XY_PLOT);
		}
		else
		{
			((DatasetRef) a.aval1).addRow ().set (0, x,
				function.evaluateDouble (x));
		}
		return false;
	}

	public static ObjectToDouble<double[]> toObjectToDouble (final DoubleToDouble f)
	{
		return new ObjectToDouble<double[]> ()
		{
			@Override
			public double evaluateDouble (double[] x)
			{
				return f.evaluateDouble (x[0]);
			}
		};
	}

	public static boolean plot (Aggregate a, double x, double y)
	{
		if (a.initialize ())
		{
			DatasetRef data = new DatasetRef ("plotData");
			data.clear ().addColumn ("Values");
			a.aval1 = data;
			a.ival = 0;
		}
		if (a.isFinished ())
		{
			chart ((DatasetRef) a.aval1, ChartPanel.XY_PLOT);
		}
		else
		{
			((DatasetRef) a.aval1).addRow ().set (0, x, y);
		}
		return false;
	}

	public static boolean plotPoints (Aggregate a, double x, double y)
	{
		if (a.initialize ())
		{
			DatasetRef data = new DatasetRef ("plotData");
			data.clear ().addColumn ("Values");
			a.aval1 = data;
			a.ival = 0;
		}
		if (a.isFinished ())
		{
			chart ((DatasetRef) a.aval1, ChartPanel.SCATTER_PLOT);
		}
		else
		{
			((DatasetRef) a.aval1).addRow ().set (0, x, y);
		}
		return false;
	}

	public static void range (DoubleConsumer consumer, double min, double max)
	{
		range (consumer, min, max, 100);
	}

	public static void range (DoubleConsumer consumer, double min, double max, int n)
	{
		for (int i = 0; i <= n; i++)
		{
			consumer.consume((min * (n - i) + max * i) / n);
		}
	}

	public static int select (Aggregate a, Node value)
	{
		if (a.initialize ())
		{
			a.aval1 = new ObjectList<Node> ();
		}
		if (a.isFinished ())
		{
			a.ival = ((ObjectList<Node>) a.aval1).size ();
			workbench ().select (((ObjectList<Node>) a.aval1).toArray (new Node[a.ival]));
		}
		else
		{
			((ObjectList<Node>) a.aval1).add (value);
		}
		return 0;
	}

	/**
	 * This finish iterator (see the XL Language Specification)
	 * is used in a <code>for</code>-statement to execute its body
	 * <code>count</code> times. Each execution is surrounded
	 * by a transformation boundary (see {@link #derive()}).
	 * 
	 * @param count number of iterations
	 * @return a finish iterator
	 */
	public static DisposableIterator apply (final int count)
	{
		return new DisposableIterator ()
		{
			final RGGGraph ex = Runtime.INSTANCE.currentGraph ();
			int i = count;

			@Override
			public boolean next ()
			{
				if (--i < 0)
				{
					return false;
				}
				ex.derive ();
				return true;
			}

			@Override
			public void dispose (Throwable t)
			{
				ex.derive ();
			}
		};
	}

	/**
	 * This finish iterator (see the XL Language Specification)
	 * is used in a <code>for</code>-statement to execute its body
	 * as long as the body makes modifications to the graph. 
	 * Each execution is surrounded
	 * by a transformation boundary (see {@link #derive()}).
	 * 
	 * @return a finish iterator
	 */
	public static DisposableIterator applyUntilFinished ()
	{
		return new DisposableIterator ()
		{
			final RGGGraph ex = Runtime.INSTANCE.currentGraph ();
			boolean notFirst = false;
			long stamp;
			int graphStamp;

			@Override
			public boolean next ()
			{
				long s = ex.derive ();
				int g = ex.getGraphManager ().getStamp ();
				if (notFirst && (s == stamp) && (g == graphStamp))
				{
					return false;
				}
				stamp = s;
				graphStamp = g;
				notFirst = true;
				return true;
			}

			@Override
			public void dispose (Throwable t)
			{
			}
		};
	}

	/**
	 * @deprecated replaced by {@link #derive()}
	 */
	@Deprecated
	public static void passBoundary ()
	{
		derive ();
	}

	/**
	 * @deprecated replaced by {@link #derive()}
	 */
	@Deprecated
	public static void apply ()
	{
		derive ();
	}

	/**
	 * This method induces a <em>transformation boundary</em>
	 * on the current RGG extent (see the XL Language Specification).
	 * This means that all pending graph modifications are
	 * applied to the graph. 
	 */
	public static void derive ()
	{
		RGGGraph ex = Runtime.INSTANCE.currentGraph ();
		ex.derive ();
	}

	public static GRSVertex newGRSVertices (ObjectConsumer<? super GRSVertex> cons)
	{
		Runtime.INSTANCE.currentGraph ().getNewGRSVertices ().evaluateObject (cons);
		return null;
	}

	public static DisposableIterator interpretiveRules ()
	{
		return new DisposableIterator ()
		{
			private final RGGGraph ex = Runtime.INSTANCE.currentGraph ();
			private boolean next = true;
			private int old;

			@Override
			public boolean next ()
			{
				if (next)
				{
					old = ex.getDerivationMode ();
					ex.setDerivationMode (old | RGGGraph.INTERPRETIVE_FLAG);
					next = false;
					return true;
				}
				return false;
			}

			@Override
			public void dispose (Throwable t)
			{
				ex.setDerivationMode (old);
			}
		};
	}

	public static void removeInterpretiveNodes ()
	{
		Runtime.INSTANCE.currentGraph ()
			.removeInterpretiveNodesOnDerivation ();
	}

	/**
	 * Sets the {@link Node#getExtentIndex() extentIndex} property of
	 * <code>root</code> and its subgraph spanned by
	 * {@link EdgePatternImpl#TREE} to <code>index</code>. If <code>ps</code>
	 * is <code>null</code>, the property is modified as part of the
	 * {@linkplain de.grogra.persistence.PersistenceManager#getActiveTransaction()
	 * active transaction} of the graph. Otherwise, the {@link PropertyQueue}
	 * of <code>ps</code> is used to enqueue the modifications.
	 * <p>
	 * Note that under normal operation, nodes at the {@link Node#LAST_EXTENT_INDEX}
	 * of the type extent are not returned as part of graph queries.
	 * 
	 * @param ps producer to obtain a modification queue, or <code>null</code>
	 * @param root root node of subgraph
	 * @param index new value of the <code>extentTail</code> property of the nodes
	 */
	public static void moveToExtent (RGGProducer ps, Node root,
			final int index)
	{
		final Transaction xa = (ps != null) ? null : root.getGraph ()
			.getActiveTransaction ();
		final PropertyQueue q = (ps != null) ? (PropertyQueue) ps
			.getQueues ().getQueue (PropertyQueue.PROPERTY_QUEUE) : null;
		VisitorImpl v = new VisitorImpl ()
		{
			@Override
			public Object visitEnter (Path path, boolean node)
			{
				if (node)
				{
					Node n = (Node) path.getObject (-1);
					if (xa != null)
					{
						Node.extentIndex$FIELD.setInt (n, null, index, xa);
					}
					else
					{
						q.setInt (n, Node.extentIndex$FIELD, null, index);
					}
				}
				return null;
			}

			@Override
			public Object visitInstanceEnter ()
			{
				return STOP;
			}
		};

		v.init (root.getCurrentGraphState (), EdgePatternImpl.TREE);
		root.getGraph ().accept (root, v, null);
	}

	/**
	 * Hides the subgraph starting at <code>root</code> so that it is no
	 * longer visible and will not be reported as part of graph queries
	 * (and left hand sides of rules, thus).
	 * <p>
	 * Technically, this behaviour is achieved by two steps: At first,
	 * <code>root</code> is reparented such that it becomes the child of
	 * the {@link RGGRoot} of this graph, the connection being established
	 * by an edge of type {@link Graph#MARK_EDGE}. This makes the subgraph
	 * invisible. Afterwards, {@link #moveToExtent} is invoked
	 * in order to move the subgraph to the last list of the type extent. Under
	 * normal operation, this excludes the subgraph from being returned
	 * as part of graph queries.
	 * <p>
	 * The modifications are made as part of the
	 * {@linkplain de.grogra.persistence.PersistenceManager#getActiveTransaction()
	 * active transaction} of the graph.
	 * 
	 * @param root root node of subgraph
	 */
	public static void hide (Node root)
	{
		Transaction xa = root.getGraph ().getActiveTransaction ();
		Node parent = root.findAdjacent (true, false, -1);
		if (parent != null)
		{
			parent.getEdgeTo (root).remove (xa);
		}
		RGGRoot.getRoot (root.getGraph ()).addEdgeBitsTo (root,
			Graph.MARK_EDGE, xa);
		moveToExtent (null, root, Node.LAST_EXTENT_INDEX);
	}
	
	/**
	 * This method replaces chains of transformation nodes
	 * with single {@link Null} nodes which perform
	 * the aggregated transformation of the chains. This is the same
	 * as <code>mergeTransformations(root, null)</code>.
	 * 
	 * @param root root node of subgraph where chains should be replaced
	 * @param allowShape store aggregated transformation in shape node at
	 * chain end if possible
	 * 
	 * @see #mergeTransformations(Node, ObjectToBoolean, boolean)
	 */
	public static void mergeTransformations (Node root, boolean allowShape)
	{
		mergeTransformations (root, null, allowShape);
	}

	/**
	 * This method replaces chains of transformation nodes
	 * with single {@link Null} node which perform
	 * the aggregated transformation of the chain; however, transformation
	 * nodes which are tropisms are not allowed to be part of the chains.
	 * This is the same
	 * as <code>mergeNonTropismTransformations(root, null)</code>.
	 * 
	 * @param root root node of subgraph where chains should be replaced
	 * @param allowShape store aggregated transformation in shape node at
	 * chain end if possible
	 * 
	 * @see #mergeNonTropismTransformations(Node, ObjectToBoolean, boolean)
	 */
	public static void mergeNonTropismTransformations (Node root,
			boolean allowShape)
	{
		mergeNonTropismTransformations (root, null, allowShape);
	}
	
	/**
	 * This method replaces chains of transformation nodes passing
	 * <code>filter</code> with single {@link Null} nodes which perform
	 * the aggregated transformation of the chains; however, transformation
	 * nodes which are tropisms are not allowed to be part of the chains.
	 * Tropisms are defined by the subclasses of {@link Tropism}.
	 * 
	 * @param root root node of subgraph where chains should be replaced
	 * @param filter only merge nodes which pass this filter. If <code>null</code>
	 * is specified, every non-tropism transformation node is considered for merging
	 * 
	 * @see #mergeTransformations(Node, ObjectToBoolean, boolean)
	 */
	public static void mergeNonTropismTransformations (Node root,
			final ObjectToBoolean<? super Node> filter, boolean allowShape)
	{
		mergeTransformations (root, new ObjectToBoolean<Node> ()
		{
			@Override
			public boolean evaluateBoolean (Node x)
			{
				return !(x instanceof Tropism)
					&& ((filter == null) || filter.evaluateBoolean (x));
			}
		}, allowShape);
	}

	/**
	 * This method replaces chains of transformation nodes passing
	 * <code>filter</code> with single {@link Null} nodes which perform
	 * the aggregated transformation of the chains. A node is considered to be
	 * a transformation node if it has a
	 * {@linkplain de.grogra.imp3d.objects.Attributes#TRANSFORMATION transformation attribute},
	 * but no
	 * {@linkplain de.grogra.imp3d.objects.Attributes#SHAPE shape attribute}.
	 * If it additionally passes <code>filter</code> and is part of a chain of
	 * such nodes, this chain will be replaced by an equivalent single node of class
	 * {@link Null}.
	 * 
	 * @param root root node of subgraph where chains should be replaced
	 * @param filter only merge nodes which pass this filter. If <code>null</code>
	 * is specified, every transformation node is considered for merging
	 * @param allowShape if possible, store aggregated transformation in
	 * existing shape node at chain end
	 * (instead of creating a new <code>Null</code> node)
	 */
	public static void mergeTransformations (Node root,
			ObjectToBoolean<? super Node> filter, boolean allowShape)
	{
		derive ();
		GraphState gs = root.getCurrentGraphState ();
		Transaction xa = root.getGraph ().getActiveTransaction ();

		// stack so that this method can be implemented non-recursive
		ObjectList<Node> nodes = new ObjectList<Node> (1000);

		// current chain of mergeable transformation nodes
		ObjectList<Node> chain = new ObjectList<Node> (100);
		// transformations of mergeable transformation nodes
		ObjectList<Transformation> xfChain = new ObjectList<Transformation> (
			100);

		// queue of chains to merge. Each entry consists of three elements: At first
		// the aggregated transformation matrix of the chain as an instance of
		// TMatrix4d, then the first node of the chain, then the last node of the chain
		ObjectList<Object> queue = new ObjectList<Object> (1000);

		nodes.push (root);

		Matrix4d tmp = new Matrix4d ();
		Matrix4d out = new Matrix4d ();
		Matrix4d res = new Matrix4d ();
		Math2.makeAffine (tmp);
		Math2.makeAffine (out);
		Math2.makeAffine (res);

		while (!nodes.isEmpty ())
		{
			Node popped = nodes.pop ();
			Node n = popped;

			boolean lastIsShape = false;
			chain.clear ();
			xfChain.clear ();

			boolean haveBranch = false;
			boolean reachedByBranch = false;

			findChain: while (n != null)
			{
				Object xf = gs.getObjectDefault (n, true,
					Attributes.TRANSFORMATION, gs);
				if (xf == gs)
				{
					// n is no transformation node
					break;
				}
				boolean hasShape = n.getAccessor (Attributes.SHAPE) != null;
				if (hasShape && !(allowShape && (n instanceof Null)))
				{
					// n has a non-mergeable shape
					break;
				}
				if ((filter != null) && !filter.evaluateBoolean (n))
				{
					// n does not pass filter
					break;
				}
				chain.add (n);
				xfChain.add ((Transformation) xf);
				haveBranch |= reachedByBranch;

				if (hasShape)
				{
					lastIsShape = true;
					// n has a shape, chain has to terminate here
					break;
				}

				// find next node
				Node next = null;
				for (Edge e = n.getFirstEdge (); e != null; e = e.getNext (n))
				{
					Node t = e.getTarget ();
					if ((t != n)
						&& e.testEdgeBits (Graph.BRANCH_EDGE
							| Graph.SUCCESSOR_EDGE))
					{
						if (next == null)
						{
							reachedByBranch = e.testEdgeBits (Graph.BRANCH_EDGE);
							next = t;
						}
						else
						{
							// more than one child exists, chain terminates
							break findChain;
						}
					}
				}
				n = next;
			}

			if (chain.size () > 1)
			{
				// compute aggregated transformation in res
				res.setIdentity ();
				for (int i = 0; i < chain.size (); i++)
				{
					n = chain.get (i);
					if (lastIsShape && (i == chain.size () - 1))
					{
						Transform3D xf = ((Null) n).getTransform ();
						if (xf != null)
						{
							xf.transform (res, res);
						}
					}
					else
					{
						Transformation xf = xfChain.get (i);
						if (xf != null)
						{
							xf.preTransform (n, true, res, tmp, gs);
							xf.postTransform (n, true, tmp, out, res, gs);
							Matrix4d m = res;
							res = out;
							out = m;
						}
					}
				}
				if (haveBranch)
				{
					res.m33 = -1;
				}

				// new entry in queue
				queue.push (new TMatrix4d (res)).push (popped).push (
					chain.pop ());
				popped = n;
			}

			// push children of popped on nodes-stack
			for (Edge e = popped.getFirstEdge (); e != null; e = e
				.getNext (popped))
			{
				Node t = e.getTarget ();
				if ((t != popped)
					&& e
						.testEdgeBits (Graph.BRANCH_EDGE | Graph.SUCCESSOR_EDGE))
				{
					nodes.push (t);
				}
			}
		}

		chain = null;
		xfChain = null;
		nodes = null;

		// apply the collected merge entries in queue
		while (!queue.isEmpty ())
		{
			Node last = (Node) queue.pop ();
			Node first = (Node) queue.pop ();
			TMatrix4d xf = (TMatrix4d) queue.pop ();
			boolean haveBranch = xf.m33 < 0;
			if (haveBranch)
			{
				xf.m33 = 1;
			}

			Node subs;
			if (last instanceof Null)
			{
				subs = last;
				Null.transform$FIELD.setObject (last, null, xf, xa);

				// remove incoming edges from last
				for (Edge e = last.getFirstEdge (); e != null; e = e
					.getNext (last))
				{
					if (e.isTarget (last))
					{
						e.remove (xa);
					}
				}
			}
			else
			{
				subs = new Null (xf);

				// reparent outgoing edges from last to subs
				for (Edge e = last.getFirstEdge (), f; e != null; e = f)
				{
					f = e.getNext (last);
					Node t = e.getTarget ();
					if (t != last)
					{
						int b = e.getEdgeBits ();
						e.remove (xa);
						subs.addEdgeBitsTo (t, b, xa);
					}
				}
			}

			// reparent incoming edges from first to subs
			for (Edge e = first.getFirstEdge (), f; e != null; e = f)
			{
				f = e.getNext (first);
				Node s = e.getSource ();
				if (s != first)
				{
					int b = e.getEdgeBits ();
					if (haveBranch && ((b & Graph.SUCCESSOR_EDGE) != 0))
					{
						b = (b & ~Graph.SUCCESSOR_EDGE) | Graph.BRANCH_EDGE;
					}
					e.remove (xa);
					s.addEdgeBitsTo (subs, b, xa);
				}
			}
		}
		derive ();
	}

	/**
	 * Removes all leaf nodes from the graph which are pure transformation
	 * nodes (see {@link #removeTransformationLeaves(Node, ObjectToBoolean)}).
	 * 
	 * @param root root node of subgraph whose leaves shall be removed
	 * 
	 * @see #removeLeaves
	 */
	public static void removeTransformationLeaves (Node root)
	{
		removeTransformationLeaves (root, null);
	}

	/**
	 * Removes all leaf nodes from the graph which are pure transformation
	 * nodes and which pass <code>filter</code>,
	 * starting at <code>root</code> (see {@link #removeLeaves}).
	 * A node is considered to be a pure transformation node if it has a
	 * {@linkplain de.grogra.imp3d.objects.Attributes#TRANSFORMATION transformation attribute},
	 * but no
	 * {@linkplain de.grogra.imp3d.objects.Attributes#SHAPE shape attribute}.
	 * 
	 * @param root root node of subgraph whose leaves shall be removed
	 * @param filter only leaf nodes which pass this filter are removed.
	 * If <code>null</code>, all transformation nodes are considered
	 * 
	 * @see #removeLeaves
	 */
	public static void removeTransformationLeaves (Node root, final ObjectToBoolean<? super Node> filter)
	{
		removeLeaves (root, new ObjectToBoolean<Node> ()
			{
				@Override
				public boolean evaluateBoolean (Node x)
				{
					if (x.getAccessor (Attributes.TRANSFORMATION) == null)
					{
						// n is no transformation node
						return false;
					}
					if (x.getAccessor (Attributes.SHAPE) != null)
					{
						// n has a shape
						return false;
					}
					return (filter == null) || filter.evaluateBoolean (x);
				}
			});
	}

	/**
	 * Removes all leaf nodes from the graph which pass
	 * <code>filter</code>, starting at <code>root</code>. A leaf is a node
	 * which can be reached from the <code>root</code> by traversing
	 * {@link #branch} or {@link #successor} edges in forward direction and
	 * which has no outgoing edge. If a node which passes <code>filter</code>
	 * is no leaf initially, but becomes a leaf due to the removal of its
	 * children, it is also removed itself. As a result, the graph finally
	 * does not contain any leaves which pass <code>filter</code>.
	 * 
	 * @param root root node of subgraph whose leaves shall be removed
	 * @param filter only leaf nodes which pass this filter are removed
	 */
	public static void removeLeaves (Node root, ObjectToBoolean<? super Node> filter)
	{
		derive ();
		Transaction xa = root.getGraph ().getActiveTransaction ();
		ObjectList<Node> stack = new ObjectList<Node> ();
		stack.push (root);
	
	loop:
		while (!stack.isEmpty ())
		{
			Node n = stack.pop ();
			if (n != null)
			{
				stack.push (n);
				stack.push (null);
				for (Edge e = n.getFirstEdge (); e != null; e = e.getNext (n))
				{
					Node t = e.getTarget ();
					if ((t != n)
						&& e.testEdgeBits (Graph.BRANCH_EDGE | Graph.SUCCESSOR_EDGE))
					{
						stack.push (t);
					}
				}
			}
			else
			{
				n = stack.pop ();
				for (Edge e = n.getFirstEdge (); e != null; e = e.getNext (n))
				{
					Node t = e.getTarget ();
					if (t != n)
					{
						continue loop;
					}
				}
				if (filter.evaluateBoolean (n))
				{
					n.removeAll (xa);
				}
			}
		}
		derive ();
	}

	/**
	 * This aggregate method computes a <code>Statistics</code>
	 * object for a series of values <code>a</code>.
	 * 
	 * @param a aggregate instance (provided by the XL compiler)
	 * @param value a value of the sequence of values
	 * @return statistics of the series of values
	 */
	public static Statistics statistics (Aggregate a, double value)
	{
		if (a.initialize ())
		{
			a.ival1 = 0;
			a.dval1 = 0;
			a.dval2 = 0;
			a.dval3 = 0;
			a.dval = Double.MAX_VALUE;
			a.dval4 = Double.MIN_VALUE;
		}
		if (!a.isFinished ())
		{
			a.ival1++;
			a.dval1 += value;
			a.dval2 += value * value;
			a.dval3 += value * value * value;
			a.dval = Math.min (a.dval, value);
			a.dval4 = Math.max (a.dval4, value);
		}
		else
		{
			a.aval = new Statistics (a.ival1, a.dval1, a.dval2, a.dval3, a.dval, a.dval4);
		}
		return null;
	}

	/**
	 * Put runnable into modification queue and execute later when derive() is called.
	 * @param r
	 */
	public static void defer (Runnable r)
	{
		Runtime.INSTANCE.currentGraph ().getQueues ().getQueue (
			GraphQueue.EXECUTE_DESCRIPTOR).execute (r);
	}

	/**
	 * Pick a random point on the surface of a unit sphere and return it as direction vector.
	 * @return random uniformly distributed direction vector
	 */
	public static Vector3d randomDirection() {
		final Vector3d result = new Vector3d();
		// generate two uniformly distributed values from [0, 1)
		double u = Operators.getRandomGenerator().nextDouble();
		double v = Operators.getRandomGenerator().nextDouble();
		// calculate coordinates based on these values
		double s = sqrt(1 - u*u);
		double theta = 2 * PI * v;
		result.x = s * cos(theta);
		result.y = s * sin(theta);
		result.z = 2 * u - 1;
		return result;
	}
}
